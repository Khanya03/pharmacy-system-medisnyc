

import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AppShell from "../components/AppShell";
import ConsultNav from "../components/ConsultNav.jsx";
import { Alert } from "../components/Field";
import StatusBadge from "../components/StatusBadge";
import { useAuth } from "../context/useAuth";
import * as appointmentApi from "../api/appointmentApi";
import { formatPatientName, getPatientDetails } from "../utils/patientLookup";
import { parseApiError } from "../utils/parseApiError";
import "./Appointments.css";
import "./Consultations.css";

function isToday(iso) {
  if (!iso) return false;
  const d = new Date(iso);
  const now = new Date();
  return (
    d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && d.getDate() === now.getDate()
  );
}

function formatTime(iso) {
  return new Date(iso).toLocaleTimeString(undefined, { hour: "2-digit", minute: "2-digit" });
}

export default function TodaysAppointments() {
  const { profileId } = useAuth();
  const [appointments, setAppointments] = useState([]);
  const [patientDetails, setPatientDetails] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showAll, setShowAll] = useState(false);
  const [confirmingId, setConfirmingId] = useState(null);

  useEffect(() => {
    appointmentApi
      .getAppointmentsByDoctor(profileId())
      .then(async (data) => {
        const sorted = [...data].sort((a, b) => new Date(a.appointmentDate) - new Date(b.appointmentDate));
        setAppointments(sorted);
        const patients = await Promise.all([...new Set(data.map((appointment) => appointment.patientId))]
          .map(async (patientId) => [patientId, await getPatientDetails(patientId)]));
        setPatientDetails(Object.fromEntries(patients));
      })
      .catch((err) => setError(parseApiError(err).message))
      .finally(() => setLoading(false));
  }, [profileId]);

  const visible = showAll ? appointments : appointments.filter((a) => isToday(a.appointmentDate));

  async function confirmAppointment(appointmentId) {
    setConfirmingId(appointmentId);
    setError("");
    try {
      const updated = await appointmentApi.updateAppointmentStatus(appointmentId, "CONFIRMED");
      setAppointments((previous) => previous.map((appointment) =>
        appointment.appointmentId === appointmentId ? updated : appointment));
    } catch (err) {
      setError(parseApiError(err).message);
    } finally {
      setConfirmingId(null);
    }
  }

  return (
    <AppShell>
      <div className="appts-header">
        <div>
          <h1>Today's appointments</h1>
          <p className="appts-sub">Start a consultation straight from a booked appointment.</p>
        </div>
        <button className="btn-secondary consult-toggle-btn" onClick={() => setShowAll((v) => !v)}>
          {showAll ? "Show today only" : "Show all upcoming"}
        </button>
      </div>

      <ConsultNav />

      <Alert type="error">{error}</Alert>

      {loading ? (
        <p className="appts-loading">Loading appointments…</p>
      ) : visible.length === 0 ? (
        <div className="appts-empty">
          <p>{showAll ? "No appointments booked with you yet." : "Nothing on today's list."}</p>
        </div>
      ) : (
        <div className="appts-list">
          {visible.map((a) => (
            <div className="appts-row" key={a.appointmentId}>
              <div className="appts-row-main">
                <div className="appts-row-id">{formatTime(a.appointmentDate)}</div>
                <div className="appts-row-parties">
                  Patient: {formatPatientName(patientDetails[a.patientId], a.patientId)}
                </div>
                {a.reason && <div className="appts-row-reason">{a.reason}</div>}
              </div>
              <div className="appts-row-side">
                <StatusBadge status={a.status} />
                {a.status === "SCHEDULED" && (
                  <button
                    type="button"
                    className="btn-secondary appts-cancel-btn"
                    disabled={confirmingId === a.appointmentId}
                    onClick={() => confirmAppointment(a.appointmentId)}
                  >
                    {confirmingId === a.appointmentId ? "Confirming…" : "Accept appointment"}
                  </button>
                )}
                <Link
                  to={`/consultations/new?appointmentId=${a.appointmentId}&patientId=${a.patientId}`}
                  className="btn-primary consult-start-btn"
                >
                  Start consultation
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </AppShell>
  );
}