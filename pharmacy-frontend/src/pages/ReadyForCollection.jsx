import { useEffect, useState } from "react";
import AppShell from "../components/AppShell";
import { Alert } from "../components/Field";
import * as processingApi from "../api/prescriptionProcessingApi";
import { parseApiError } from "../utils/parseApiError";
import "./Appointments.css";
import "./Prescriptions.css";

const POLL_INTERVAL_MS = 10000;

export default function ReadyForCollection() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [collectingId, setCollectingId] = useState(null);

    function load() {
        processingApi
            .getReadyForCollection()
            .then(setItems)
            .catch((err) => setError(parseApiError(err).message))
            .finally(() => setLoading(false));
    }

    useEffect(() => {
        load();
        const timer = setInterval(() => {
            // Don't refetch out from under an in-flight collect click.
            setCollectingId((current) => {
                if (!current) load();
                return current;
            });
        }, POLL_INTERVAL_MS);
        return () => clearInterval(timer);
    }, []);

    async function handleCollect(prescriptionId) {
        setCollectingId(prescriptionId);
        setError("");
        try {
            await processingApi.advanceStatus(prescriptionId);
            setItems((prev) => prev.filter((p) => p.prescriptionId !== prescriptionId));
        } catch (err) {
            setError(parseApiError(err).message);
        } finally {
            setCollectingId(null);
        }
    }

    return (
        <AppShell>
            <h1>Ready for collection</h1>
            <p className="appts-sub" style={{ marginBottom: "24px" }}>
                Prescriptions that are ready. Click <strong>Mark collected</strong> on the right when the patient collects it.
            </p>

            <Alert type="error">{error}</Alert>

            {loading ? (
                <p className="appts-loading">Loading…</p>
            ) : items.length === 0 ? (
                <div className="appts-empty">
                    <p>Nothing waiting for collection right now.</p>
                </div>
            ) : (
                <div className="appts-list">
                    {items.map((p) => (
                        <div className="appts-row" key={p.prescriptionId}>
                            <div className="appts-row-main">
                                <div className="appts-row-id">{p.prescriptionId}</div>
                                <div className="appts-row-parties">Patient: {p.patientId}</div>
                            </div>
                            <div className="appts-row-side">
                                <button
                                    className="rx-queue-advance-btn"
                                    onClick={() => handleCollect(p.prescriptionId)}
                                    disabled={collectingId === p.prescriptionId}
                                >
                                    {collectingId === p.prescriptionId ? "Collecting…" : "Mark as collected"}
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </AppShell>
    );
}