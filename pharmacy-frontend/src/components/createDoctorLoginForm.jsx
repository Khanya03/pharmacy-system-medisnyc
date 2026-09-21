
components
CreateDoctorLoginForm.jsx
import { useState } from "react";
import { Field, Alert } from "./Field";
import * as adminApi from "../api/adminApi";
import { parseApiError } from "../utils/parseApiError";

export default function CreateDoctorLoginForm({ doctor, onDone, onCancel }) {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState(doctor.email || "");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await adminApi.createDoctorLogin({ doctorId: doctor.doctorId, username, email, password });
      onDone();
    } catch (err) {
      setError(parseApiError(err).message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="book-card" style={{ marginBottom: "16px", maxWidth: "480px" }}>
      <p style={{ marginTop: 0, fontWeight: 600 }}>
        Create login for Dr. {doctor.firstName} {doctor.lastName}
      </p>
      <p className="helper-text" style={{ marginTop: "-6px" }}>
        Linked automatically to Doctor ID {doctor.doctorId} — no manual linking needed afterwards.
      </p>
      <Alert type="error">{error}</Alert>
      <Field id="doctorLoginUsername" label="Username" value={username} onChange={(e) => setUsername(e.target.value)} required />
      <Field id="doctorLoginEmail" label="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
      <Field
        id="doctorLoginPassword"
        label="Password"
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        required
      />
      <div style={{ display: "flex", gap: "10px" }}>
        <button className="btn-primary" type="submit" disabled={submitting} style={{ width: "auto", padding: "10px 20px" }}>
          {submitting ? "Creating…" : "Create login"}
        </button>
        <button
          type="button"
          className="btn-secondary"
          onClick={onCancel}
          style={{ width: "auto", padding: "10px 20px" }}
        >
          Cancel
        </button>
      </div>
    </form>
  );
}