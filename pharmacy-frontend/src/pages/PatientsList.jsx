

import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import AppShell from "../components/AppShell";
import AdminNav from "../components/AdminNav";
import { Alert } from "../components/Field";
import * as patientApi from "../api/patientApi";
import { parseApiError } from "../utils/parseApiError";
import "./Admin.css";
import "./Appointments.css";
import "./Inventory.css";

export default function PatientsList() {
  const location = useLocation();
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    patientApi
      .getAllPatients()
      .then(setPatients)
      .catch((err) => setError(parseApiError(err).message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <AppShell>
      <div className="appts-header">
        <div>
          <h1>Patients</h1>
          <p className="appts-sub">Everyone registered in the system, including walk-ins.</p>
        </div>
        <Link to="/admin/patients/add" className="btn-primary inv-add-btn">
          + Add patient
        </Link>
      </div>

      <AdminNav />

      <Alert type="error">{error}</Alert>
      {location.state?.justAdded && <Alert type="success">Patient added.</Alert>}

      {loading ? (
        <p className="appts-loading">Loading patients…</p>
      ) : patients.length === 0 ? (
        <div className="appts-empty">
          <p>No patients yet. They'll appear here once someone registers, or you add a walk-in below.</p>
        </div>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Date of birth</th>
              <th>Medical ID</th>
            </tr>
          </thead>
          <tbody>
            {patients.map((p) => (
              <tr key={p.medicalId}>
                <td>
                  {p.firstName} {p.lastName}
                </td>
                <td>{p.email}</td>
                <td>{p.phoneNumber}</td>
                <td>{p.dateOfBirth}</td>
                <td style={{ fontFamily: "var(--font-mono)", fontSize: "12px", color: "var(--ink-soft)" }}>
                  {p.medicalId}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </AppShell>
  );
}