import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import AppShell from "../components/AppShell";
import AdminNav from "../components/AdminNav";
import { Alert } from "../components/Field";
import CreateDoctorLoginForm from "../components/CreateDoctorLoginForm.jsx";
import * as doctorApi from "../api/doctorApi";
import { parseApiError } from "../utils/parseApiError";
import "./Admin.css";
import "./Appointments.css";
import "./Inventory.css";

const EMPTY_EDIT = { firstName: "", lastName: "", specialization: "", phoneNumber: "", email: "" };

export default function DoctorsList() {
    const location = useLocation();
    const [doctors, setDoctors] = useState([]);
    const [specializations, setSpecializations] = useState([]);
    const [specializationFilter, setSpecializationFilter] = useState("");
    const [loadedFilter, setLoadedFilter] = useState(null);
    const [error, setError] = useState("");
    const [loginFormFor, setLoginFormFor] = useState(null);
    const [justCreatedLogin, setJustCreatedLogin] = useState(false);

    const [editingId, setEditingId] = useState(null);
    const [editForm, setEditForm] = useState(EMPTY_EDIT);
    const [editError, setEditError] = useState("");
    const [busyId, setBusyId] = useState(null);

    useEffect(() => {
        doctorApi.getSpecializations()
            .then(setSpecializations)
            .catch((err) => setError(parseApiError(err).message))
    }, []);

    useEffect(() => {
        let cancelled = false;
        doctorApi
            .getDoctors(specializationFilter || undefined)
            .then((result) => {
                if (!cancelled) setDoctors(result);
            })
            .catch((err) => {
                if (!cancelled) setError(parseApiError(err).message);
            })
            .finally(() => {
                if (!cancelled) setLoadedFilter(specializationFilter);
            });
        return () => {
            cancelled = true;
        };
    }, [specializationFilter]);
    const loading = loadedFilter !== specializationFilter;

    function startEdit(doctor) {
        setEditingId(doctor.doctorId);
        setEditForm({
            firstName: doctor.firstName || "",
            lastName: doctor.lastName || "",
            specialization: doctor.specialization || "",
            phoneNumber: doctor.phoneNumber || "",
            email: doctor.email || "",
        });
        setEditError("");
    }

    function cancelEdit() {
        setEditingId(null);
        setEditError("");
    }

    async function saveEdit(doctorId) {
        setBusyId(doctorId);
        setEditError("");
        try {
            const updated = await doctorApi.updateDoctor(doctorId, editForm);
            setDoctors((previous) => previous.flatMap((doctor) => {
                if (doctor.doctorId !== doctorId) return [doctor];
                if (specializationFilter &&
                    updated.specialization?.toLowerCase() !== specializationFilter.toLowerCase()) {
                    return [];
                }
                return [updated];
            }));
            setEditingId(null);
        } catch (err) {
            setEditError(parseApiError(err).message);
        } finally {
            setBusyId(null);
        }
    }

    async function handleDelete(doctor) {
        if (!window.confirm(`Delete Dr. ${doctor.firstName} ${doctor.lastName}? This can't be undone.`)) return;
        setBusyId(doctor.doctorId);
        setError("");
        try {
            await doctorApi.deleteDoctor(doctor.doctorId);
            setDoctors((prev) => prev.filter((d) => d.doctorId !== doctor.doctorId));
        } catch (err) {
            // Deletion is blocked (appointments/prescriptions/records/login still
            // reference this doctor) - surface exactly why instead of failing silently.
            setError(parseApiError(err).message);
        } finally {
            setBusyId(null);
        }
    }

    return (
        <AppShell>
            <div className="appts-header">
                <div>
                    <h1>Doctors</h1>
                    <p className="appts-sub">Everyone patients can book an appointment with.</p>
                </div>
                <Link to="/admin/doctors/add" className="btn-primary inv-add-btn">
                    + Add doctor
                </Link>
            </div>

            <AdminNav />

            <Alert type="error">{error}</Alert>
            {location.state?.justAdded && <Alert type="success">Doctor added.</Alert>}
            {justCreatedLogin && <Alert type="success">Login created. The doctor can now sign in directly.</Alert>}

            <div className="doctor-directory-filter">
                <label className="field-label" htmlFor="doctor-specialization-filter">Filter by specialization</label>
                <select
                    id="doctor-specialization-filter"
                    className="field-select"
                    value={specializationFilter}
                    onChange={(event) => {
                        setError("");
                        setSpecializationFilter(event.target.value);
                    }}
                >
                    <option value="">All doctors</option>
                    {specializations.map((specialization) => (
                        <option key={specialization} value={specialization}>{specialization}</option>
                    ))}
                </select>
            </div>

            {loginFormFor && (
                <CreateDoctorLoginForm
                    doctor={loginFormFor}
                    onCancel={() => setLoginFormFor(null)}
                    onDone={() => {
                        setLoginFormFor(null);
                        setJustCreatedLogin(true);
                    }}
                />
            )}

            {loading ? (
                <p className="appts-loading">Loading doctors…</p>
            ) : doctors.length === 0 ? (
                <div className="appts-empty">
                    <p>No doctors yet. Add one so patients can start booking appointments.</p>
                </div>
            ) : (
                <table className="admin-table">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Specialization</th>
                        <th>Phone</th>
                        <th>Email</th>
                        <th>Doctor ID</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {doctors.map((d) =>
                        editingId === d.doctorId ? (
                            <tr key={d.doctorId}>
                                <td>
                                    <div style={{ display: "flex", gap: "4px" }}>
                                        <input
                                            className="admin-linked-id-input"
                                            style={{ width: "70px" }}
                                            value={editForm.firstName}
                                            onChange={(e) => setEditForm((p) => ({ ...p, firstName: e.target.value }))}
                                            placeholder="First"
                                        />
                                        <input
                                            className="admin-linked-id-input"
                                            style={{ width: "70px" }}
                                            value={editForm.lastName}
                                            onChange={(e) => setEditForm((p) => ({ ...p, lastName: e.target.value }))}
                                            placeholder="Last"
                                        />
                                    </div>
                                </td>
                                <td>
                                    <select
                                        className="admin-linked-id-input"
                                        value={editForm.specialization}
                                        onChange={(e) => setEditForm((p) => ({ ...p, specialization: e.target.value }))}
                                    >
                                        {!specializations.includes(editForm.specialization) && editForm.specialization && (
                                            <option value={editForm.specialization}>{editForm.specialization} (legacy)</option>
                                        )}
                                        <option value="">Choose specialization</option>
                                        {specializations.map((specialization) => (
                                            <option key={specialization} value={specialization}>{specialization}</option>
                                        ))}
                                    </select>
                                </td>
                                <td>
                                    <input
                                        className="admin-linked-id-input"
                                        style={{ width: "100px" }}
                                        value={editForm.phoneNumber}
                                        onChange={(e) => setEditForm((p) => ({ ...p, phoneNumber: e.target.value }))}
                                    />
                                </td>
                                <td>
                                    <input
                                        className="admin-linked-id-input"
                                        style={{ width: "150px" }}
                                        value={editForm.email}
                                        onChange={(e) => setEditForm((p) => ({ ...p, email: e.target.value }))}
                                    />
                                </td>
                                <td style={{ fontFamily: "var(--font-mono)", fontSize: "12px", color: "var(--ink-soft)" }}>
                                    {d.doctorId}
                                </td>
                                <td>
                                    {editError && <div className="link-profile-error" style={{ marginBottom: "4px" }}>{editError}</div>}
                                    <button className="admin-toggle-btn" disabled={busyId === d.doctorId} onClick={() => saveEdit(d.doctorId)}>
                                        {busyId === d.doctorId ? "Saving…" : "Save"}
                                    </button>
                                    <button className="admin-toggle-btn" disabled={busyId === d.doctorId} onClick={cancelEdit}>
                                        Cancel
                                    </button>
                                </td>
                            </tr>
                        ) : (
                            <tr key={d.doctorId}>
                                <td>
                                    Dr. {d.firstName} {d.lastName}
                                </td>
                                <td>{d.specialization}</td>
                                <td>{d.phoneNumber}</td>
                                <td>{d.email}</td>
                                <td style={{ fontFamily: "var(--font-mono)", fontSize: "12px", color: "var(--ink-soft)" }}>
                                    {d.doctorId}
                                </td>
                                <td>
                                    <button className="admin-toggle-btn" disabled={busyId === d.doctorId} onClick={() => setLoginFormFor(d)}>
                                        Create login
                                    </button>
                                    <button className="admin-toggle-btn" disabled={busyId === d.doctorId} onClick={() => startEdit(d)}>
                                        Edit
                                    </button>
                                    <button
                                        className="admin-delete-btn"
                                        disabled={busyId === d.doctorId}
                                        onClick={() => handleDelete(d)}
                                    >
                                        {busyId === d.doctorId ? "Deleting…" : "Delete"}
                                    </button>
                                </td>
                            </tr>
                        )
                    )}
                    </tbody>
                </table>
            )}
        </AppShell>
    );
}