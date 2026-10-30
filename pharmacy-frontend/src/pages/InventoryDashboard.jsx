import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import AppShell from "../components/AppShell";
import { Alert } from "../components/Field";
import * as medicationApi from "../api/medicationApi";
import * as inventoryApi from "../api/inventoryApi";
import { parseApiError } from "../utils/parseApiError";
import "./Inventory.css";

const ADJUSTMENT_REASONS = [
  ["RECEIVED_CORRECTION", "Received stock correction"],
  ["DAMAGED", "Damaged"],
  ["EXPIRED_WRITE_OFF", "Expired write-off"],
  ["STOCK_TAKE", "Stock take"],
  ["OTHER", "Other"],
];

function StockAdjustmentDialog({ item, onClose, onSave }) {
  const [amount, setAmount] = useState("1");
  const [direction, setDirection] = useState("ADD");
  const [reason, setReason] = useState("STOCK_TAKE");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const delta = Number(amount) * (direction === "ADD" ? 1 : -1);

  async function submit(e) {
    e.preventDefault();
    setError("");
    setSaving(true);
    try {
      await onSave(item.inventoryId, delta, reason);
      onClose();
    } catch (err) {
      setError(parseApiError(err).message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="inv-dialog-backdrop" role="presentation" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <form className="inv-dialog" onSubmit={submit} aria-labelledby="adjust-title">
        <h2 id="adjust-title">Adjust batch stock</h2>
        <p>{item.batchNumber} · current stock: {item.stockLevel}</p>
        <Alert>{error}</Alert>
        <div className="field-row">
          <label className="field">
            <span className="field-label">Change</span>
            <select className="field-select" value={direction} onChange={(e) => setDirection(e.target.value)}>
              <option value="ADD">Add stock (+)</option>
              <option value="REMOVE">Remove stock (−)</option>
            </select>
          </label>
          <label className="field">
            <span className="field-label">Amount</span>
            <input className="field-input" type="number" min="1" step="1" required value={amount} onChange={(e) => setAmount(e.target.value)} />
          </label>
        </div>
        <label className="field">
          <span className="field-label">Reason</span>
          <select className="field-select" value={reason} onChange={(e) => setReason(e.target.value)}>
            {ADJUSTMENT_REASONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
        </label>
        <p className={`inv-preview ${item.stockLevel + delta < 0 ? "inv-preview-error" : ""}`}>
          Preview: {item.stockLevel} {delta >= 0 ? "+" : "−"} {Math.abs(delta)} = {item.stockLevel + delta}
        </p>
        <div className="inv-dialog-actions">
          <button className="btn-secondary" type="button" onClick={onClose}>Cancel</button>
          <button className="btn-primary" type="submit" disabled={saving || !Number.isInteger(Number(amount)) || Number(amount) <= 0}>
            {saving ? "Saving…" : "Apply adjustment"}
          </button>
        </div>
      </form>
    </div>
  );
}

export default function InventoryDashboard() {
  const [medications, setMedications] = useState([]);
  const [inventory, setInventory] = useState([]);
  const [summary, setSummary] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [adjustingItem, setAdjustingItem] = useState(null);
  const [roles] = useState(() => {
    try {
      return (JSON.parse(localStorage.getItem("rx_user"))?.roles || []).map((role) => role.replace(/^ROLE_/, ""));
    } catch {
      return [];
    }
  });
  const canWriteOff = roles.includes("ADMIN") || roles.includes("PHARMACIST");

  function fetchAll() {
    return Promise.all([
      medicationApi.getAllMedications(),
      inventoryApi.getAllInventory(),
      inventoryApi.getInventorySummary(),
    ]);
  }

  function applyInventoryData([meds, batches, totals]) {
    setMedications(meds);
    setInventory(batches);
    setSummary(totals);
  }

  async function loadAll() {
    try {
      applyInventoryData(await fetchAll());
    } catch (err) {
      setError(parseApiError(err).message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    let mounted = true;
    fetchAll()
      .then((data) => {
        if (mounted) applyInventoryData(data);
      })
      .catch((err) => {
        if (mounted) setError(parseApiError(err).message);
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });
    return () => { mounted = false; };
  }, []);

  async function handleAdjust(inventoryId, delta, reason) {
    await inventoryApi.adjustStock(inventoryId, delta, reason);
    await loadAll();
  }

  async function handleWriteOff(item) {
    if (!window.confirm(`Write off the remaining ${item.stockLevel} units in expired batch ${item.batchNumber}?`)) return;
    setError("");
    try {
      await inventoryApi.writeOffExpiredStock(item.inventoryId, item.stockLevel);
      await loadAll();
    } catch (err) {
      setError(parseApiError(err).message);
    }
  }

  async function handleDelete(medication) {
    if (!window.confirm(`Delete ${medication.medicationName}? This cannot be undone.`)) return;
    setError("");
    try {
      await medicationApi.deleteMedication(medication.medicationId);
      await loadAll();
    } catch (err) {
      setError(parseApiError(err).message);
    }
  }

  const medicationName = (id) => medications.find((m) => m.medicationId === id)?.medicationName || id;
  const [{ today, soon }] = useState(() => {
    const current = new Date();
    const cutoff = new Date(current.getTime() + 30 * 86400000);
    return {
      today: current.toISOString().slice(0, 10),
      soon: cutoff.toISOString().slice(0, 10),
    };
  });
  const expiredCount = summary.reduce((count, med) => count + med.expiredBatchCount, 0);
  const expiringCount = summary.reduce((count, med) => count + med.expiringSoonBatchCount, 0);

  const visibleSummary = useMemo(() => summary.filter((med) => {
    const matchesName = med.medicationName.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === "ALL"
      || med.status === statusFilter
      || (statusFilter === "EXPIRED" && med.expiredBatchCount > 0)
      || (statusFilter === "EXPIRING_SOON" && med.expiringSoonBatchCount > 0);
    return matchesName && matchesStatus;
  }), [summary, search, statusFilter]);

  const issueBanners = [
    { status: "LOW", count: summary.filter((med) => med.status === "LOW").length, label: "medicines at or below reorder level", tone: "warning" },
    { status: "OUT", count: summary.filter((med) => med.status === "OUT").length, label: "medicines out of stock", tone: "error" },
    { status: "EXPIRED", count: expiredCount, label: "expired batches requiring write-off", tone: "error" },
    { status: "EXPIRING_SOON", count: expiringCount, label: "batches expiring within 30 days", tone: "warning" },
  ];

  return (
    <AppShell>
      <div className="inventory-banner">
        <img src="/images/shelf.jpg" alt="Organised pharmacy shelves" />
        <div>
          <h1>Pharmacy inventory</h1>
          <p className="appts-sub">Manage the medicine catalogue and keep stock levels current.</p>
        </div>
      </div>

      <Alert type="error">{error}</Alert>

      {!loading && issueBanners.filter((banner) => banner.count > 0).map((banner) => (
        <button
          type="button"
          key={banner.status}
          className={`inv-alert-banner inv-alert-${banner.tone}`}
          onClick={() => setStatusFilter(banner.status)}
        >
          <span><strong>{banner.count}</strong> {banner.label}</span>
          <span className="inv-banner-link">Filter table →</span>
        </button>
      ))}

      {loading ? (
        <p className="appts-loading">Loading inventory…</p>
      ) : (
        <>
          <div className="inv-section">
            <div className="inv-section-head">
              <h2>Inventory summary</h2>
              <Link to="/inventory/add" className="btn-primary inv-add-btn">+ Add medicine</Link>
            </div>
            <div className="inv-filters">
              <input className="inv-filter-input" aria-label="Search medicines by name" placeholder="Search medicine name…" value={search} onChange={(e) => setSearch(e.target.value)} />
              <select className="inv-filter-input" aria-label="Filter by inventory status" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
                <option value="ALL">All statuses</option>
                <option value="OK">OK</option>
                <option value="LOW">Low</option>
                <option value="OUT">Out of stock</option>
                <option value="EXPIRING_SOON">Expiring soon</option>
                <option value="EXPIRED">Expired</option>
              </select>
            </div>
            {visibleSummary.length === 0 ? (
              <p className="inv-empty">No medicines match the selected search and filter.</p>
            ) : (
              <table className="inv-table">
                <thead><tr><th>Medicine</th><th>Form</th><th>Strength</th><th>Total stock</th><th>Status</th><th>Earliest expiry</th></tr></thead>
                <tbody>
                  {visibleSummary.map((med) => (
                    <tr key={med.medicationId}>
                      <td>{med.medicationName}</td>
                      <td>{med.dosageForm}</td>
                      <td>{med.strength}</td>
                      <td>{med.totalStock}</td>
                      <td><span className={`inv-status inv-status-${med.status.toLowerCase()}`}>{med.status.replaceAll("_", " ")}</span></td>
                      <td>{med.earliestExpiry || "—"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          <div className="inv-section">
            <div className="inv-section-head"><h2>Medicine catalogue</h2></div>
            {medications.length === 0 ? (
              <p className="inv-empty">No medications in the catalogue yet.</p>
            ) : (
              <table className="inv-table">
                <thead><tr><th>Name</th><th>Form</th><th>Strength</th><th>Manufacturer</th><th>Category</th><th>Actions</th></tr></thead>
                <tbody>
                  {medications.map((med) => (
                    <tr key={med.medicationId}>
                      <td>{med.medicationName}</td><td>{med.dosageForm || "—"}</td><td>{med.strength || "—"}</td>
                      <td>{med.manufacturer || "—"}</td><td>{med.category || "—"}</td>
                      <td className="inv-actions">
                        <Link to={`/inventory/add?medicationId=${encodeURIComponent(med.medicationId)}`}>Receive stock</Link>
                        <Link to={`/inventory/add?edit=${encodeURIComponent(med.medicationId)}`}>Edit</Link>
                        <button type="button" className="inv-text-button" onClick={() => handleDelete(med)}>Delete</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          <div className="inv-section">
            <div className="inv-section-head"><h2>Stock batches</h2></div>
            {inventory.length === 0 ? (
              <p className="inv-empty">No stock received yet.</p>
            ) : (
              <table className="inv-table">
                <thead><tr><th>Medicine</th><th>Batch</th><th>Stock</th><th>Reorder level</th><th>Expiry</th><th>Actions</th></tr></thead>
                <tbody>
                  {inventory.map((item) => {
                    const isExpired = item.expiryDate < today;
                    const isExpiring = !isExpired && item.stockLevel > 0 && item.expiryDate <= soon;
                    return (
                      <tr key={item.inventoryId} className={isExpired ? "inv-batch-expired" : isExpiring ? "inv-batch-expiring" : ""}>
                        <td>{medicationName(item.medicationId)}</td>
                        <td>{item.batchNumber || "—"}</td><td>{item.stockLevel}</td><td>{item.reorderLevel}</td>
                        <td>{item.expiryDate || "—"} {isExpired && <span className="inv-date-label inv-date-expired">Expired</span>}{isExpiring && <span className="inv-date-label inv-date-expiring">Expiring soon</span>}</td>
                        <td className="inv-actions">
                          <button type="button" className="inv-text-button" onClick={() => setAdjustingItem(item)}>Adjust</button>
                          {isExpired && item.stockLevel > 0 && canWriteOff && <button type="button" className="inv-text-button inv-danger-button" onClick={() => handleWriteOff(item)}>Write off expired</button>}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
      {adjustingItem && <StockAdjustmentDialog item={adjustingItem} onClose={() => setAdjustingItem(null)} onSave={handleAdjust} />}
    </AppShell>
  );
}
