
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AppShell from "../components/AppShell";
import { Alert } from "../components/Field";
import StatusBadge from "../components/StatusBadge";
import { useAuth } from "../context/useAuth";
import * as appointmentApi from "../api/appointmentApi";
import * as doctorApi from "../api/doctorApi";
import { formatPatientName, getPatientDetails } from "../utils/patientLookup";
import { parseApiError } from "../utils/parseApiError";
import "./Appointments.css";

function formatDateTime(iso) {
    if (!iso) return "";
    return new Date(iso).toLocaleString(undefined, {
        weekday: "short",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
}

function fetchForRole(role, id) {
    if (role === "DOCTOR") return appointmentApi.getAppointmentsByDoctor(id);
    if (role === "ADMIN" || role === "PHARMACIST") return appointmentApi.getAllAppointments();
    return appointmentApi.getAppointmentsByPatient(id);
}

// This page is the appointment *list* — linked from every dashboard
// (patient: "your appointments", doctor: "all your appointments",
// admin/pharmacist: "every appointment in the system"). Booking a new
// appointment happens on a separate page: /appointments/book.
export default function Appointments() {
    const { profileId, primaryRole } = useAuth();
    const role = primaryRole();
    const id = profileId();

    const [appointments, setAppointments] = useState([]);
    const [doctorNames, setDoctorNames] = useState({});
    const [patientNames, setPatientNames] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [cancellingId, setCancellingId] = useState(null);

    useEffect(() => {
        Promise.all([fetchForRole(role, id), doctorApi.getAllDoctors().catch(() => [])])
            .then(async ([apts, doctors]) => {
                const names = {};
                doctors.forEach((d) => {
                    names[d.doctorId] = `Dr. ${d.firstName} ${d.lastName}`;
                });
                setDoctorNames(names);
                if (role !== "PATIENT") {
                    const patients = await Promise.all([...new Set(apts.map((appointment) => appointment.patientId))]
                        .map(async (patientId) => [patientId, await getPatientDetails(patientId)]));
                    setPatientNames(Object.fromEntries(patients));
                }
                const sorted = [...apts].sort((a, b) => new Date(b.appointmentDate) - new Date(a.appointmentDate));
                setAppointments(sorted);
            })
            .catch((err) => setError(parseApiError(err).message))
            .finally(() => setLoading(false));
    }, [role, id]);

    async function handleCancel(appointmentId) {
        setCancellingId(appointmentId);
        setError("");
        try {
            const updated = await appointmentApi.cancelAppointment(appointmentId);
            setAppointments((prev) => prev.map((a) => (a.appointmentId === appointmentId ? updated : a)));
        } catch (err) {
            setError(parseApiError(err).message);
        } finally {
            setCancellingId(null);
        }
    }

    const isStaffView = role === "ADMIN" || role === "PHARMACIST";
    const heading = role === "PATIENT" ? "Your appointments" : role === "DOCTOR" ? "All your appointments" : "Every appointment in the system";
    const sub =
        role === "PATIENT"
            ? "Book a new appointment, or view and cancel upcoming ones."
            : role === "DOCTOR"
                ? "Everything booked with you, past and upcoming."
                : "Every appointment booked across the practice.";

    return (
        <AppShell>
            <div className="appts-header">
                <div>
                    <h1>{heading}</h1>
                    <p className="appts-sub">{sub}</p>
                </div>
                {role === "PATIENT" && (
                    <Link to="/appointments/book" className="btn-primary appts-book-btn">
                        Book appointment
                    </Link>
                )}
            </div>

            <Alert type="error">{error}</Alert>

            {loading ? (
                <p className="appts-loading">Loading appointments…</p>
            ) : appointments.length === 0 ? (
                <div className="appts-empty">
                    <p>
                        {role === "PATIENT"
                            ? "You haven't booked an appointment yet."
                            : role === "DOCTOR"
                                ? "No appointments booked with you yet."
                                : "No appointments have been booked yet."}
                    </p>
                    {role === "PATIENT" && (
                        <Link to="/appointments/book" className="btn-primary appts-book-btn">
                            Book your first appointment
                        </Link>
                    )}
                </div>
            ) : (
                <div className="appts-list">
                    {appointments.map((a) => (
                        <div className="appts-row" key={a.appointmentId}>
                            <div className="appts-row-main">
                                <div className="appts-row-id">{formatDateTime(a.appointmentDate)}</div>
                                <div className="appts-row-parties">
                                    {role === "PATIENT" && <>Doctor: {doctorNames[a.doctorId] || a.doctorId}</>}
                                    {role === "DOCTOR" && <>Patient: {formatPatientName(patientNames[a.patientId], a.patientId)}</>}
                                    {isStaffView && (
                                        <>
                                            Patient: {formatPatientName(patientNames[a.patientId], a.patientId)}
                                            &nbsp;·&nbsp; Doctor: {doctorNames[a.doctorId] || a.doctorId}
                                        </>
                                    )}
                                </div>
                                {a.reason && <div className="appts-row-reason">{a.reason}</div>}
                            </div>
                            <div className="appts-row-side">
                                <StatusBadge status={a.status} />
                                {role === "PATIENT" && (a.status === "SCHEDULED" || a.status === "CONFIRMED") && (
                                    <button
                                        type="button"
                                        className="btn-secondary appts-cancel-btn"
                                        disabled={cancellingId === a.appointmentId}
                                        onClick={() => handleCancel(a.appointmentId)}
                                    >
                                        {cancellingId === a.appointmentId ? "Cancelling…" : "Cancel"}
                                    </button>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </AppShell>
    );
}