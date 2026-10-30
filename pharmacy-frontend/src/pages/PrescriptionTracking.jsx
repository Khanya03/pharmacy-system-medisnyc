import { useEffect, useState } from "react";
import AppShell from "../components/AppShell";
import { Alert } from "../components/Field";
import { useAuth } from "../context/useAuth";
import * as processingApi from "../api/prescriptionProcessingApi";
import { parseApiError } from "../utils/parseApiError";
import "./Appointments.css";
import "./Prescriptions.css";

const POLL_INTERVAL_MS = 10000;

const STEPS = [
    { key: "PENDING", label: "Pending" },
    { key: "RECEIVED", label: "Received" },
    { key: "PREPARING", label: "Preparing" },
    { key: "READY_FOR_COLLECTION", label: "Ready" },
    { key: "COLLECTED", label: "Collected" },
];

function TrackRail({ status }) {
    const currentIndex = STEPS.findIndex((s) => s.key === status);
    return (
        <div className="rx-track-rail">
            {STEPS.map((step, i) => (
                <div
                    key={step.key}
                    className={`rx-track-step ${i < currentIndex ? "done" : ""} ${i === currentIndex ? "current" : ""}`}
                >
                    <div className="rx-track-connector" />
                    <div className="rx-track-step-dot">{i < currentIndex ? "✓" : i + 1}</div>
                    <div className="rx-track-step-label">{step.label}</div>
                </div>
            ))}
        </div>
    );
}

export default function PrescriptionTracking() {
    const { profileId } = useAuth();
    const [prescriptions, setPrescriptions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let cancelled = false;

        function load() {
            processingApi
                .getByPatient(profileId())
                .then((data) => {
                    if (!cancelled) setPrescriptions(data);
                })
                .catch((err) => {
                    if (!cancelled) setError(parseApiError(err).message);
                })
                .finally(() => {
                    if (!cancelled) setLoading(false);
                });
        }

        load();
        const timer = setInterval(load, POLL_INTERVAL_MS);
        return () => {
            cancelled = true;
            clearInterval(timer);
        };
    }, [profileId]);

    return (
        <AppShell>
            <h1>Track your prescriptions</h1>
            <p className="appts-sub" style={{ marginBottom: "24px" }}>
                Follow each prescription from pending to ready for collection.
            </p>

            <Alert type="error">{error}</Alert>

            {loading ? (
                <p className="appts-loading">Loading…</p>
            ) : prescriptions.length === 0 ? (
                <div className="appts-empty">
                    <p>No prescriptions on file yet.</p>
                </div>
            ) : (
                prescriptions.map((p) => (
                    <div className="rx-track-card" key={p.prescriptionId}>
                        <div className="rx-track-head">
                            <span className="rx-track-id">{p.prescriptionId}</span>
                            <span className="rx-track-meta">
                Issued {p.dateIssued} · Doctor: {p.doctorId}
                                {p.expiryDate ? ` · Expires ${p.expiryDate}` : ""}
              </span>
                        </div>
                        <TrackRail status={p.processingStatus} />
                    </div>
                ))
            )}
        </AppShell>
    );
}
