import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import AppShell from "../components/AppShell";
import { Alert } from "../components/Field";
import StatusBadge from "../components/StatusBadge";
import * as processingApi from "../api/prescriptionProcessingApi";
import * as orderApi from "../api/orderApi";
import { parseApiError } from "../utils/parseApiError";
import "./Appointments.css";
import "./Prescriptions.css";
import "./Payments.css";

const POLL_INTERVAL_MS = 10000;
const STATUSES = ["PENDING", "RECEIVED", "PREPARING", "READY_FOR_COLLECTION", "COLLECTED"];

const NEXT_LABEL = {
    PENDING: "Mark received",
    RECEIVED: "Start preparing",
    PREPARING: "Mark ready for collection",
    READY_FOR_COLLECTION: "Mark collected",
};

function processingStatus(prescription) {
    return prescription.processingStatus || "PENDING";
}

function issuedDate(date) {
    if (!date) return "—";
    return new Date(`${date}T00:00:00`).toLocaleDateString();
}

export default function PrescriptionQueue() {
    const [queue, setQueue] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [advancingId, setAdvancingId] = useState(null);
    const [rowErrors, setRowErrors] = useState({});
    const [toast, setToast] = useState(null);
    const [search, setSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState("ALL");
    const advanceInFlightRef = useRef(false);
    const toastRef = useRef(null);

    const load = useCallback(async () => {
        try {
            const items = await processingApi.getQueue();
            const enriched = await Promise.all(items.map(async (prescription) => {
                if (!prescription.linkedOrderId) return prescription;
                const order = await orderApi.getOrderById(prescription.linkedOrderId);
                return { ...prescription, linkedOrder: order };
            }));
            setQueue(enriched);
            setError("");
        } catch (err) {
            setError(parseApiError(err).message);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        queueMicrotask(load);
        const timer = setInterval(() => {
            if (!advanceInFlightRef.current) load();
        }, POLL_INTERVAL_MS);
        return () => clearInterval(timer);
    }, [load]);

    useEffect(() => {
        if (toast) toastRef.current?.scrollIntoView({ behavior: "smooth", block: "center" });
    }, [toast]);

    const visibleQueue = useMemo(() => {
        const query = search.trim().toLowerCase();
        return queue.filter((prescription) => {
            const status = processingStatus(prescription);
            const matchesStatus = statusFilter === "ALL" || status === statusFilter;
            const matchesSearch = !query || [
                prescription.prescriptionId,
                prescription.patientName,
                prescription.patientId,
                prescription.doctorName,
                prescription.doctorId,
            ].some((value) => value?.toLowerCase().includes(query));
            return matchesStatus && matchesSearch;
        });
    }, [queue, search, statusFilter]);

    async function handleAdvance(prescriptionId) {
        if (advanceInFlightRef.current) return;

        advanceInFlightRef.current = true;
        setAdvancingId(prescriptionId);
        setRowErrors((previous) => {
            const next = { ...previous };
            delete next[prescriptionId];
            return next;
        });
        setToast(null);

        try {
            const updated = await processingApi.advanceStatus(prescriptionId);
            setQueue((previous) => updated.processingStatus === "COLLECTED"
                ? previous.filter((item) => item.prescriptionId !== prescriptionId)
                : previous.map((item) => item.prescriptionId === prescriptionId
                    ? { ...item, ...updated }
                    : item));
            await load();
        } catch (err) {
            const message = parseApiError(err).message;
            const medicineNames = Array.isArray(err?.response?.data?.outOfStockMedicines)
                ? err.response.data.outOfStockMedicines
                : [];
            const failure = { message, medicineNames };
            setRowErrors((previous) => ({ ...previous, [prescriptionId]: failure }));
            setToast({ prescriptionId, ...failure });
            await load();
        } finally {
            advanceInFlightRef.current = false;
            setAdvancingId(null);
        }
    }

    return (
        <AppShell>
            <h1>Prescription queue</h1>
            <p className="appts-sub" style={{ marginBottom: "24px" }}>
                Pending → Received → Preparing → Ready for Collection → Collected.
            </p>

            {toast && (
                <div className="rx-queue-toast" role="alert" aria-live="assertive" ref={toastRef}>
                    <div>
                        <strong>{toast.message}</strong>
                        {toast.medicineNames.length > 0 && (
                            <div>Out of stock: {toast.medicineNames.join(", ")}</div>
                        )}
                    </div>
                    <button type="button" onClick={() => setToast(null)} aria-label="Dismiss error">×</button>
                </div>
            )}

            <Alert type="error">{error}</Alert>

            <div className="rx-queue-filters">
                <label className="rx-queue-filter">
                    <span>Search patient, doctor, or RX ID</span>
                    <input
                        type="search"
                        value={search}
                        onChange={(event) => setSearch(event.target.value)}
                        placeholder="Name or prescription ID"
                    />
                </label>
                <label className="rx-queue-filter rx-queue-status-filter">
                    <span>Processing status</span>
                    <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
                        <option value="ALL">All statuses</option>
                        {STATUSES.map((status) => (
                            <option key={status} value={status}>
                                {status.replaceAll("_", " ")}
                            </option>
                        ))}
                    </select>
                </label>
            </div>

            {loading ? (
                <p className="appts-loading">Loading queue…</p>
            ) : visibleQueue.length === 0 ? (
                <div className="appts-empty">
                    <p>{queue.length === 0
                        ? "Nothing in the queue — everything's been collected."
                        : "No prescriptions match the current search and filters."}</p>
                </div>
            ) : (
                <div className="appts-list">
                    {visibleQueue.map((prescription) => {
                        const status = processingStatus(prescription);
                        const rowError = rowErrors[prescription.prescriptionId];
                        const inactive = prescription.status !== "ACTIVE";
                        const paymentDue = prescription.linkedOrder
                            && (prescription.linkedOrder.paymentStatus || "UNPAID") !== "PAID";
                        const cannotCollect = status === "READY_FOR_COLLECTION" && paymentDue;
                        return (
                            <div className="appts-row rx-queue-row" key={prescription.prescriptionId}>
                                <div className="appts-row-main">
                                    <div className="appts-row-id">{prescription.prescriptionId}</div>
                                    <div className="appts-row-parties">
                                        Patient: {prescription.patientName || "Unknown patient"}
                                        <span className="rx-queue-secondary-id">{prescription.patientId}</span>
                                    </div>
                                    <div className="appts-row-parties">
                                        Doctor: {prescription.doctorName || "Unknown doctor"}
                                        <span className="rx-queue-secondary-id">{prescription.doctorId}</span>
                                    </div>
                                    <div className="appts-row-parties">Issued: {issuedDate(prescription.dateIssued)}</div>
                                    <ul className="rx-queue-medicines">
                                        {(prescription.medicines || []).map((medicine) => (
                                            <li key={`${medicine.medicationId}-${medicine.quantity}`}>
                                                {medicine.name} × {medicine.quantity}
                                            </li>
                                        ))}
                                    </ul>
                                    {prescription.linkedOrderId && (
                                        <div className="appts-row-parties">Order: {prescription.linkedOrderId}</div>
                                    )}
                                    {paymentDue && (
                                        <div className="rx-queue-payment-due">
                                            Payment due R{Number(prescription.linkedOrder.totalAmount || 0).toFixed(2)}
                                        </div>
                                    )}
                                </div>
                                <div className="appts-row-side rx-queue-actions">
                                    <StatusBadge status={status} />
                                    <button
                                        className="rx-queue-advance-btn"
                                        onClick={() => handleAdvance(prescription.prescriptionId)}
                                        disabled={Boolean(advancingId) || inactive || !NEXT_LABEL[status] || cannotCollect}
                                    >
                                        {advancingId === prescription.prescriptionId
                                            ? "Updating…"
                                            : NEXT_LABEL[status] || "No further action"}
                                    </button>
                                    {rowError && (
                                        <div className="rx-queue-row-error" role="alert">
                                            <div>{rowError.message}</div>
                                            {rowError.medicineNames.length > 0 && (
                                                <div>Out of stock: {rowError.medicineNames.join(", ")}</div>
                                            )}
                                        </div>
                                    )}
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </AppShell>
    );
}
