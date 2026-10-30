import { useEffect, useState } from "react";
import AppShell from "../components/AppShell";
import { Alert } from "../components/Field";
import StatusBadge from "../components/StatusBadge";
import { useAuth } from "../context/useAuth";
import * as processingApi from "../api/prescriptionProcessingApi";
import { parseApiError } from "../utils/parseApiError";
import "./Appointments.css";
import "./Prescriptions.css";

const POLL_INTERVAL_MS = 10000;

export default function DoctorPrescriptions() {
    const { profileId } = useAuth();
    const [prescriptions, setPrescriptions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let cancelled = false;

        function load() {
            processingApi
                .getByDoctor(profileId())
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
            <h1>Prescriptions you've issued</h1>
            <p className="appts-sub" style={{ marginBottom: "24px" }}>
                See where each prescription sits in the pharmacy pipeline — this list updates automatically.
            </p>

            <Alert type="error">{error}</Alert>

            {loading ? (
                <p className="appts-loading">Loading…</p>
            ) : prescriptions.length === 0 ? (
                <div className="appts-empty">
                    <p>You haven't issued any prescriptions yet.</p>
                </div>
            ) : (
                <div className="appts-list">
                    {prescriptions.map((p) => (
                        <div className="appts-row" key={p.prescriptionId}>
                            <div className="appts-row-main">
                                <div className="appts-row-id">{p.prescriptionId}</div>
                                <div className="appts-row-parties">Patient: {p.patientId}</div>
                                <div className="appts-row-parties">
                                    Issued {p.dateIssued}
                                    {p.expiryDate ? ` · Expires ${p.expiryDate}` : ""}
                                </div>
                            </div>
                            <div className="appts-row-side">
                                <StatusBadge status={p.processingStatus} />
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </AppShell>
    );
}