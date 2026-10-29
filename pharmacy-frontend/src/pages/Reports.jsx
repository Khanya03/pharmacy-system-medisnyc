
import { useCallback, useEffect, useState } from "react";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  LineChart, Line } from "recharts";
import AppShell from "../components/AppShell";
import AdminNav from "../components/AdminNav";
import { Alert } from "../components/Field";
import * as adminApi from "../api/adminApi.js";
import * as paymentApi from "../api/paymentApi.js";
import StatusBadge from "../components/StatusBadge";
import { parseApiError } from "../utils/parseApiError";
import "./Admin.css";
import "./Payments.css";

const CHART_LABELS = {
  totalPatients: "Patients",
  totalAppointments: "Appointments",
  totalPrescriptions: "Prescriptions",
  totalOrders: "Orders",
  lowStockItems: "Low stock",
  prescriptionsAwaitingCollection: "Awaiting collection",
};

export default function Reports() {
  const [report, setReport] = useState(null);
  const [stats, setStats] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [from, setFrom] = useState(() => dateString(new Date(Date.now() - 29 * 86400000)));
  const [to, setTo] = useState(() => dateString(new Date()));
  const [payments, setPayments] = useState(null);
  const [paymentPage, setPaymentPage] = useState(0);
  const [paymentStatus, setPaymentStatus] = useState("");
  const [paymentLoading, setPaymentLoading] = useState(true);
  const [exporting, setExporting] = useState(false);

  const loadReport = useCallback(() => {
    setError("");
    Promise.all([adminApi.getRevenueReport({ from, to }), adminApi.getDashboardStatistics()])
      .then(([revenue, dashboardStats]) => {
        setReport(revenue);
        setStats(dashboardStats);
      })
      .catch((err) => setError(parseApiError(err).message))
      .finally(() => setLoading(false));
  }, [from, to]);

  const loadPayments = useCallback(() => {
    setError("");
    paymentApi.getPayments({ from, to, status: paymentStatus, page: paymentPage, size: 10 })
      .then(setPayments)
      .catch((err) => setError(parseApiError(err).message))
      .finally(() => setPaymentLoading(false));
  }, [from, to, paymentStatus, paymentPage]);

  useEffect(() => { queueMicrotask(loadReport); }, [loadReport]);
  useEffect(() => { queueMicrotask(loadPayments); }, [loadPayments]);

  function quickRange(range) {
    const now = new Date();
    const start = new Date(now);
    if (range === "Today") start.setHours(0, 0, 0, 0);
    if (range === "7 days") start.setDate(now.getDate() - 6);
    if (range === "30 days") start.setDate(now.getDate() - 29);
    if (range === "This month") start.setDate(1);
    setFrom(dateString(start));
    setTo(dateString(now));
    setPaymentPage(0);
    setLoading(true);
    setPaymentLoading(true);
  }

  async function handleExport() {
    setExporting(true);
    setError("");
    try {
      const blob = await paymentApi.exportPayments({ from, to });
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement("a");
      anchor.href = url;
      anchor.download = "payments.csv";
      document.body.appendChild(anchor);
      anchor.click();
      anchor.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      setError(parseApiError(err).message);
    } finally {
      setExporting(false);
    }
  }

  const chartData = stats
    ? Object.entries(CHART_LABELS).map(([key, label]) => ({ name: label, value: stats[key] || 0 }))
    : [];

  return (
    <AppShell>
      <div className="appts-eyebrow">Module 6</div>
      <h1>Reports</h1>
      <p className="appts-sub" style={{ marginBottom: "20px" }}>
        Revenue and system-wide activity.
      </p>

      <AdminNav />

      <Alert type="error">{error}</Alert>

      <div className="reports-date-controls">
        <label>From <input type="date" value={from} max={to} onChange={(event) => {
          setFrom(event.target.value); setPaymentPage(0); setLoading(true); setPaymentLoading(true);
        }} /></label>
        <label>To <input type="date" value={to} min={from} onChange={(event) => {
          setTo(event.target.value); setPaymentPage(0); setLoading(true); setPaymentLoading(true);
        }} /></label>
        <div className="reports-quick-ranges">
          {["Today", "7 days", "30 days", "This month"].map((range) => (
            <button type="button" className="admin-toggle-btn" key={range} onClick={() => quickRange(range)}>{range}</button>
          ))}
        </div>
      </div>

      {loading ? (
        <p className="appts-loading">Loading report…</p>
      ) : !report ? (
        <div className="appts-empty"><p>Revenue report could not be loaded.</p>
          <button type="button" className="admin-toggle-btn" onClick={() => {
            setLoading(true);
            loadReport();
          }}>Retry report</button>
        </div>
      ) : (
        <>
          <div className="stat-grid">
            {[
              ["Net revenue", `R${money(report.totalRevenue)}`],
              ["Gross paid", `R${money(report.grossPaid)}`],
              ["Refunds", `R${money(report.refunds)}`],
              ["Outstanding", `R${money(report.outstandingAmount)}`],
              ["Paid orders", report.paidOrders || 0],
              ["Unpaid orders", report.unpaidOrders || 0],
              ["Failed payments", report.failedPayments || 0],
              ["Average order value", `R${money(report.averageOrderValue)}`],
            ].map(([label, value]) => (
              <div className="stat-card" key={label}>
                <div className="stat-card-value">{value}</div>
                <div className="stat-card-label">{label}</div>
              </div>
            ))}
          </div>

          <div className="reports-chart-grid">
            <section className="inv-section">
              <div className="inv-section-head"><h2>Revenue by day</h2></div>
              {(report.byDay || []).length === 0 ? <p className="reports-empty">No payments in this period</p> : (
                <div className="reports-chart">
                  <ResponsiveContainer width="100%" height={260}>
                    <LineChart data={report.byDay}>
                      <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                      <XAxis dataKey="date" tick={{ fontSize: 11, fill: "#4b5d55" }} />
                      <YAxis tick={{ fontSize: 11, fill: "#4b5d55" }} />
                      <Tooltip formatter={(value) => `R${money(value)}`} />
                      <Line type="monotone" dataKey="amount" stroke="#1f4b3f" strokeWidth={3} dot={false} />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              )}
            </section>
            <section className="inv-section">
              <div className="inv-section-head"><h2>Revenue by card brand</h2></div>
              {(report.byCardBrand || []).length === 0 ? <p className="reports-empty">No payments in this period</p> : (
                <div className="reports-chart">
                  <ResponsiveContainer width="100%" height={260}>
                    <BarChart data={report.byCardBrand}>
                      <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                      <XAxis dataKey="brand" tick={{ fontSize: 11, fill: "#4b5d55" }} />
                      <YAxis tick={{ fontSize: 11, fill: "#4b5d55" }} />
                      <Tooltip formatter={(value) => `R${money(value)}`} />
                      <Bar dataKey="amount" fill="#1f4b3f" radius={[6, 6, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              )}
            </section>
          </div>

          <section className="inv-section reports-payments">
            <div className="inv-section-head">
              <h2>Recent payments</h2>
              <div className="reports-payment-tools">
                <select value={paymentStatus} onChange={(event) => {
                  setPaymentStatus(event.target.value); setPaymentPage(0); setPaymentLoading(true);
                }} aria-label="Filter payment status">
                  <option value="">All statuses</option>
                  {["PAID", "FAILED", "REFUNDED", "PENDING"].map((status) => <option key={status}>{status}</option>)}
                </select>
                <button className="admin-toggle-btn" type="button" disabled={exporting} onClick={handleExport}>
                  {exporting ? "Exporting…" : "Export CSV"}
                </button>
              </div>
            </div>
            {paymentLoading ? <p className="appts-loading">Loading payments…</p>
              : !payments?.content?.length ? <p className="reports-empty">No payments in this period</p>
                : <>
                  <div className="reports-table-scroll">
                    <table className="admin-table">
                      <thead><tr><th>Date</th><th>Receipt</th><th>Order</th><th>Patient</th><th>Amount</th><th>Card</th><th>Status</th></tr></thead>
                      <tbody>{payments.content.map((payment) => (
                        <tr key={payment.paymentId}>
                          <td>{payment.createdAt ? new Date(payment.createdAt).toLocaleDateString() : "—"}</td>
                          <td>{payment.receiptNumber || "—"}</td><td>{payment.orderId}</td><td>{payment.patientId}</td>
                          <td>R{money(payment.amount)}</td>
                          <td>{payment.cardBrand || "—"} {payment.cardLast4 ? `•••• ${payment.cardLast4}` : ""}</td>
                          <td><StatusBadge status={payment.status} /></td>
                        </tr>
                      ))}</tbody>
                    </table>
                  </div>
                  <div className="reports-pagination">
                    <span>Page {(payments.number || 0) + 1} of {Math.max(payments.totalPages || 1, 1)}</span>
                    <button className="admin-toggle-btn" type="button" disabled={paymentPage === 0}
                      onClick={() => { setPaymentPage((page) => page - 1); setPaymentLoading(true); }}>Previous</button>
                    <button className="admin-toggle-btn" type="button"
                      disabled={paymentPage + 1 >= (payments.totalPages || 1)}
                      onClick={() => { setPaymentPage((page) => page + 1); setPaymentLoading(true); }}>Next</button>
                  </div>
                </>}
          </section>

          <div className="inv-section">
            <div className="inv-section-head">
              <h2>Activity overview</h2>
            </div>
            <div style={{ background: "var(--paper-raised)", border: "1px solid var(--border)", borderRadius: "14px", padding: "20px" }}>
              <ResponsiveContainer width="100%" height={280}>
                <BarChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                  <XAxis dataKey="name" tick={{ fontSize: 12, fill: "#4b5d55" }} angle={-15} textAnchor="end" height={60} />
                  <YAxis tick={{ fontSize: 12, fill: "#4b5d55" }} allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="value" fill="#1f4b3f" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </>
      )}
    </AppShell>
  );
}

function dateString(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function money(value) {
  return Number(value || 0).toFixed(2);
}