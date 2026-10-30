
import { useEffect, useState } from "react";
import AppShell from "../components/AppShell";
import AdminNav from "../components/AdminNav";
import { Alert, Field } from "../components/Field";
import * as adminApi from "../api/adminApi.js";
import { parseApiError } from "../utils/parseApiError";
import "./Admin.css";

export default function AuditLog() {
    const [logs, setLogs] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [filters, setFilters] = useState({ username: "", action: "" });
    const [appliedFilters, setAppliedFilters] = useState({ username: "", action: "" });

    function load(targetPage, nextFilters = appliedFilters) {
        setLoading(true);
        setError("");
        adminApi
            .getAuditLogs({ page: targetPage, size: 25, ...nextFilters })
            .then((data) => {
                setLogs(data.content);
                setPage(data.page);
                setTotalPages(data.totalPages);
            })
            .catch((err) => setError(parseApiError(err).message))
            .finally(() => setLoading(false));
    }

    useEffect(() => {
        let active = true;
        adminApi
            .getAuditLogs({ page: 0, size: 25 })
            .then((data) => {
                if (!active) return;
                setLogs(data.content);
                setPage(data.page);
                setTotalPages(data.totalPages);
            })
            .catch((err) => {
                if (active) setError(parseApiError(err).message);
            })
            .finally(() => {
                if (active) setLoading(false);
            });
        return () => {
            active = false;
        };
    }, []);

    function applyFilters(event) {
        event.preventDefault();
        setAppliedFilters(filters);
        load(0, filters);
    }

    return (
        <AppShell>
            <h1>Audit log</h1>
            <p className="appts-sub" style={{ marginBottom: "20px" }}>
                A record of actions taken across the system.
            </p>

            <AdminNav />

            <Alert type="error">{error}</Alert>

            <form onSubmit={applyFilters} className="book-card" style={{ display: "flex", gap: "12px", alignItems: "end", marginBottom: "18px" }}>
                <Field
                    label="Username"
                    value={filters.username}
                    onChange={(event) => setFilters((prev) => ({ ...prev, username: event.target.value }))}
                />
                <Field
                    label="Action"
                    value={filters.action}
                    onChange={(event) => setFilters((prev) => ({ ...prev, action: event.target.value }))}
                />
                <button className="btn-primary" type="submit" style={{ width: "auto" }}>Filter</button>
            </form>

            {loading ? (
                <p className="appts-loading">Loading audit log…</p>
            ) : logs.length === 0 ? (
                <p className="inv-empty">No audit entries yet.</p>
            ) : (
                <table className="admin-table">
                    <thead>
                    <tr>
                        <th>When</th>
                        <th>User</th>
                        <th>Action</th>
                        <th>Entity</th>
                        <th>Details</th>
                    </tr>
                    </thead>
                    <tbody>
                    {logs.map((log) => (
                        <tr key={log.logId}>
                            <td>{new Date(log.timestamp).toLocaleString()}</td>
                            <td>{log.username}</td>
                            <td>{log.action}</td>
                            <td>{log.entityAffected}</td>
                            <td>{log.details}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
            {!loading && (
                <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "14px", marginTop: "16px" }}>
                    <button className="btn-secondary" disabled={page <= 0} onClick={() => load(page - 1)}>
                        Previous
                    </button>
                    <span>Page {totalPages === 0 ? 0 : page + 1} of {totalPages}</span>
                    <button className="btn-secondary" disabled={page + 1 >= totalPages} onClick={() => load(page + 1)}>
                        Next
                    </button>
                </div>
            )}
        </AppShell>
    );
}