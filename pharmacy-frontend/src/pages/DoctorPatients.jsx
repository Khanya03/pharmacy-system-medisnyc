
import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import AppShell from "../components/AppShell";
import ConsultNav from "../components/ConsultNav.jsx";
import { Alert } from "../components/Field";
import { useAuth } from "../context/useAuth";
import * as appointmentApi from "../api/appointmentApi";
import { formatPatientName, getPatientDetails } from "../utils/patientLookup";
import { parseApiError } from "../utils/parseApiError";
import "./Appointments.css";
import "./Admin.css";

export default function DoctorPatients() {
    const { profileId } = useAuth();
    const [appointments, setAppointments] = useState([]);
    const [patientDetails, setPatientDetails] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        appointmentApi
            .getAppointmentsByDoctor(profileId())
            .then(async (data) => {
                setAppointments(data);
                const patients = await Promise.all([...new Set(data.map((appointment) => appointment.patientId))]
                    .map(async (patientId) => [patientId, await getPatientDetails(patientId)]));
                setPatientDetails(Object.fromEntries(patients));
            })
            .catch((err) => setError(parseApiError(err).message))
            .finally(() => setLoading(false));
    }, [profileId]);

    const patients = useMemo(() => {
        const byPatient = new Map();
        for (const a of appointments) {
            const existing = byPatient.get(a.patientId);
            const date = new Date(a.appointmentDate);
            if (!existing || date > existing.lastVisit) {
                byPatient.set(a.patientId, {
                    patientId: a.patientId,
                    visitCount: (existing?.visitCount || 0) + 1,
                    lastVisit: date,
                    lastReason: a.reason,
                });
            } else {
                existing.visitCount += 1;
            }
        }
        return [...byPatient.values()].sort((a, b) => b.lastVisit - a.lastVisit);
    }, [appointments]);

    return (
        <AppShell>
            <h1>Your patients</h1>
            <p className="appts-sub" style={{ marginBottom: "20px" }}>
                Everyone who's booked an appointment with you.
            </p>

            <ConsultNav />

            <Alert type="error">{error}</Alert>

            {loading ? (
                <p className="appts-loading">Loading patients…</p>
            ) : patients.length === 0 ? (
                <div className="appts-empty">
                    <p>No patients yet — they'll show up here once someone books with you.</p>
                </div>
            ) : (
                <table className="admin-table">
                    <thead>
                    <tr>
                        <th>Patient</th>
                        <th>Visits</th>
                        <th>Last visit</th>
                        <th>Last reason</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    {patients.map((p) => (
                        <tr key={p.patientId}>
                            <td>{formatPatientName(patientDetails[p.patientId], p.patientId)}</td>
                            <td>{p.visitCount}</td>
                            <td>{p.lastVisit.toLocaleDateString()}</td>
                            <td>{p.lastReason || "—"}</td>
                            <td>
                                <Link
                                    to={`/consultations/new?patientId=${p.patientId}`}
                                    style={{ fontSize: "13px", fontWeight: 600, textDecoration: "none" }}
                                >
                                    Start consultation
                                </Link>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </AppShell>
    );
}