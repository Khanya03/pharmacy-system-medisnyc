import { useEffect, useState } from "react";
import AppShell from "../components/AppShell";
import AdminNav from "../components/AdminNav";
import { Alert } from "../components/Field";
import * as adminApi from "../api/adminApi.js";
import { parseApiError } from "../utils/parseApiError";
import "./Admin.css";

const LABELS = {
    totalPatients: "Patients",
    totalAppointments: "Appointments",
    totalPrescriptions: "Prescriptions",
    totalOrders: "Orders",
    lowStockItems: "Low stock items",
    prescriptionsAwaitingCollection: "Awaiting collection",
};

export default function AdminOverview() {
    const [stats, setStats] = useState(null);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        adminApi
            .getDashboardStatistics()
            .then(setStats)
            .catch((err) => setError(parseApiError(err).message))
            .finally(() => setLoading(false));
    }, []);

    return (
        <AppShell>
            <h1>Admin dashboard</h1>
            <p className="appts-sub" style={{ marginBottom: "20px" }}>
                System-wide statistics, at a glance.
            </p>

            <AdminNav />

            <Alert type="error">{error}</Alert>

            {loading ? (
                <p className="appts-loading">Loading statistics…</p>
            ) : (
                <div className="stat-grid">
                    {Object.entries(LABELS).map(([key, label]) => (
                        <div
                            className={`stat-card ${
                                (key === "lowStockItems" || key === "prescriptionsAwaitingCollection") && stats[key] > 0 ? "warn" : ""
                            }`}
                            key={key}
                        >
                            <div className="stat-card-value">{stats[key] ?? "—"}</div>
                            <div className="stat-card-label">{label}</div>
                        </div>
                    ))}
                </div>
            )}
        </AppShell>
    );
}
