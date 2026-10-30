

import { useEffect, useState } from "react";
import AppShell from "../components/AppShell";
import AdminNav from "../components/AdminNav";
import { Alert, SelectField } from "../components/Field";
import CreateDoctorLoginForm from "../components/CreateDoctorLoginForm.jsx";
import * as adminApi from "../api/adminApi.js";
import * as doctorApi from "../api/doctorApi";
import { parseApiError } from "../utils/parseApiError";
import { useAuth } from "../context/useAuth";
import "./Admin.css";
import "./Appointments.css";

const ROLES = ["PATIENT", "DOCTOR", "ADMIN"];

// ADMIN/PHARMACIST accounts have no domain profile to link — only these two
// roles are ever expected to carry a linkedProfileId.
const LINKABLE_ROLES = new Set(["PATIENT", "DOCTOR"]);

function primaryRoleName(user) {
  const role = (user.roles || [])[0];
  return role ? role.replace(/^ROLE_/, "") : "";
}

export default function UserManagement() {
  const { user: loggedInUser } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  const [doctors, setDoctors] = useState([]);
  const [showDoctorLoginPicker, setShowDoctorLoginPicker] = useState(false);
  const [selectedDoctorId, setSelectedDoctorId] = useState("");
  const [justCreatedLogin, setJustCreatedLogin] = useState(false);

  // Inline "link profile" mini-form state — at most one row open at a time.
  const [linkingUserId, setLinkingUserId] = useState(null);
  const [linkForm, setLinkForm] = useState({ phoneNumber: "", dateOfBirth: "", doctorId: "" });
  const [linkError, setLinkError] = useState("");
  const [linkSubmitting, setLinkSubmitting] = useState(false);

  const selectedDoctor = doctors.find((d) => d.doctorId === selectedDoctorId);

  function load() {
    adminApi
      .getAllUsers()
      .then(setUsers)
      .catch((err) => setError(parseApiError(err).message))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);
  useEffect(() => {
    doctorApi.getAllDoctors().then(setDoctors).catch(() => setDoctors([]));
  }, []);

  function openLinkForm(userId) {
    setLinkingUserId(userId);
    setLinkForm({ phoneNumber: "", dateOfBirth: "", doctorId: "" });
    setLinkError("");
  }

  function closeLinkForm() {
    setLinkingUserId(null);
    setLinkError("");
  }

  async function handleToggleEnabled(user) {
    if (user.username === loggedInUser?.username && user.enabled) return;
    if (user.enabled && !window.confirm(`Disable ${user.username}? They will no longer be able to sign in.`)) return;
    setBusyId(user.id);
    setError("");
    try {
      const updated = await adminApi.setUserEnabled(user.id, !user.enabled);
      setUsers((prev) => prev.map((u) => (u.id === user.id ? updated : u)));
    } catch (err) {
      setError(parseApiError(err).message);
    } finally {
      setBusyId(null);
    }
  }

  async function handleAssignRole(user, role) {
    if (!role) return;
    if (user.username === loggedInUser?.username) return;
    setBusyId(user.id);
    setError("");
    try {
      const updated = await adminApi.assignRole(user.id, role);
      setUsers((prev) => prev.map((u) => (u.id === user.id ? updated : u)));
      // Closing the gap where a role change used to leave an account silently
      // unlinked: if the new role needs a profile and doesn't have one yet,
      // walk straight into the same inline form used for backfilling.
      if (LINKABLE_ROLES.has(role) && !updated.linkedProfileId) {
        openLinkForm(user.id);
      }
    } catch (err) {
      setError(parseApiError(err).message);
    } finally {
      setBusyId(null);
    }
  }

  async function handleDelete(user) {
    if (user.username === loggedInUser?.username) return;
    if (!window.confirm(`Delete ${user.username}? This will soft-disable the account and preserve its history.`)) return;
    setBusyId(user.id);
    setError("");
    try {
      const updated = await adminApi.deleteUser(user.id);
      setUsers((prev) => prev.map((u) => (u.id === user.id ? updated : u)));
    } catch (err) {
      setError(parseApiError(err).message);
    } finally {
      setBusyId(null);
    }
  }

  async function handleLinkPatient(user) {
    if (!linkForm.phoneNumber.trim() || !linkForm.dateOfBirth) {
      setLinkError("Phone number and date of birth are both required.");
      return;
    }
    setLinkSubmitting(true);
    setLinkError("");
    try {
      const updated = await adminApi.linkNewPatientProfile(user.id, {
        phoneNumber: linkForm.phoneNumber.trim(),
        dateOfBirth: linkForm.dateOfBirth,
      });
      setUsers((prev) => prev.map((u) => (u.id === user.id ? updated : u)));
      closeLinkForm();
    } catch (err) {
      setLinkError(parseApiError(err).message);
    } finally {
      setLinkSubmitting(false);
    }
  }

  async function handleLinkDoctor(user) {
    if (!linkForm.doctorId) {
      setLinkError("Choose a doctor to link this account to.");
      return;
    }
    setLinkSubmitting(true);
    setLinkError("");
    try {
      const updated = await adminApi.linkExistingDoctorProfile(user.id, linkForm.doctorId);
      setUsers((prev) => prev.map((u) => (u.id === user.id ? updated : u)));
      closeLinkForm();
    } catch (err) {
      setLinkError(parseApiError(err).message);
    } finally {
      setLinkSubmitting(false);
    }
  }

  function renderLinkedProfileCell(user) {
    const role = primaryRoleName(user);

    if (!LINKABLE_ROLES.has(role)) {
      return <span className="linked-id-value" style={{ color: "var(--ink-soft)" }}>—</span>;
    }

    if (linkingUserId === user.id) {
      return (
        <div className="link-profile-form">
          {role === "PATIENT" ? (
            <>
              <input
                type="tel"
                placeholder="Phone number"
                value={linkForm.phoneNumber}
                onChange={(e) => setLinkForm((prev) => ({ ...prev, phoneNumber: e.target.value }))}
                disabled={linkSubmitting}
              />
              <input
                type="date"
                value={linkForm.dateOfBirth}
                onChange={(e) => setLinkForm((prev) => ({ ...prev, dateOfBirth: e.target.value }))}
                disabled={linkSubmitting}
              />
            </>
          ) : (
            <select
              value={linkForm.doctorId}
              onChange={(e) => setLinkForm((prev) => ({ ...prev, doctorId: e.target.value }))}
              disabled={linkSubmitting}
            >
              <option value="">Select a doctor…</option>
              {doctors.map((d) => (
                <option key={d.doctorId} value={d.doctorId}>
                  Dr. {d.firstName} {d.lastName} ({d.doctorId})
                </option>
              ))}
            </select>
          )}
          {linkError && <div className="link-profile-error">{linkError}</div>}
          <div className="link-profile-form-actions">
            <button
              className="link-profile-save-btn"
              disabled={linkSubmitting}
              onClick={() => (role === "PATIENT" ? handleLinkPatient(user) : handleLinkDoctor(user))}
            >
              {linkSubmitting ? "Saving…" : "Save"}
            </button>
            <button className="link-profile-cancel-btn" disabled={linkSubmitting} onClick={closeLinkForm}>
              Cancel
            </button>
          </div>
        </div>
      );
    }

    if (user.linkedProfileId) {
      return (
        <div>
          <span className="linked-id-value">{user.linkedProfileId}</span>
          <button className="linked-id-change-btn" onClick={() => openLinkForm(user.id)}>
            Change
          </button>
        </div>
      );
    }

    return (
      <div>
        <span className="pill-unlinked">Not linked</span>
        <button className="link-profile-btn" onClick={() => openLinkForm(user.id)}>
          Link profile
        </button>
      </div>
    );
  }

  return (
    <AppShell>
      <div className="appts-header">
        <div>
          <h1>User management</h1>
          <p className="appts-sub" style={{ marginBottom: "20px" }}>
            Enable, disable, or change the role of any account.
          </p>
        </div>
        <button className="btn-primary inv-add-btn" onClick={() => setShowDoctorLoginPicker((v) => !v)}>
          {showDoctorLoginPicker ? "Cancel" : "+ Add doctor login"}
        </button>
      </div>

      <AdminNav />

      <Alert type="error">{error}</Alert>
      {justCreatedLogin && <Alert type="success">Login created. The doctor can now sign in directly.</Alert>}

      {showDoctorLoginPicker && !selectedDoctor && (
        <div className="book-card" style={{ marginBottom: "16px", maxWidth: "480px" }}>
          <SelectField
            label="Which doctor is this login for?"
            value={selectedDoctorId}
            onChange={(e) => setSelectedDoctorId(e.target.value)}
          >
            <option value="">Select a doctor…</option>
            {doctors.map((d) => (
              <option key={d.doctorId} value={d.doctorId}>
                Dr. {d.firstName} {d.lastName} ({d.doctorId})
              </option>
            ))}
          </SelectField>
          {doctors.length === 0 && (
            <p className="helper-text">No doctors yet — add one from the Doctors tab first.</p>
          )}
        </div>
      )}

      {showDoctorLoginPicker && selectedDoctor && (
        <CreateDoctorLoginForm
          doctor={selectedDoctor}
          onCancel={() => {
            setShowDoctorLoginPicker(false);
            setSelectedDoctorId("");
          }}
          onDone={() => {
            setShowDoctorLoginPicker(false);
            setSelectedDoctorId("");
            setJustCreatedLogin(true);
            load();
          }}
        />
      )}

      {loading ? (
        <p className="appts-loading">Loading users…</p>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>Username</th>
              <th>Email</th>
              <th>Roles</th>
              <th>Linked profile ID</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.username}</td>
                <td>{u.email}</td>
                <td>{(u.roles || []).map((role) => role.replace(/^ROLE_/, "")).join(", ")}</td>
                <td>{renderLinkedProfileCell(u)}</td>
                <td>
                  <span className={u.enabled ? "pill-enabled" : "pill-disabled"}>
                    {u.enabled ? "Enabled" : "Disabled"}
                  </span>
                </td>
                <td>
                  <select
                    className="admin-role-select"
                    defaultValue=""
                    disabled={busyId === u.id || u.username === loggedInUser?.username}
                    onChange={(e) => handleAssignRole(u, e.target.value)}
                  >
                    <option value="">Assign role…</option>
                    {ROLES.map((r) => (
                      <option key={r} value={r}>
                        {r}
                      </option>
                    ))}
                  </select>
                  <button
                    className="admin-toggle-btn"
                    disabled={busyId === u.id || (u.username === loggedInUser?.username && u.enabled)}
                    onClick={() => handleToggleEnabled(u)}
                  >
                    {u.enabled ? "Disable" : "Enable"}
                  </button>
                  <button className="admin-delete-btn" disabled={busyId === u.id || u.username === loggedInUser?.username} onClick={() => handleDelete(u)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </AppShell>
  );
}