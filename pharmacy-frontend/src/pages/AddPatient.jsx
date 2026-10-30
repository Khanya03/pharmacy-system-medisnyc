import { useState } from "react";
import { useNavigate } from "react-router-dom";
import AppShell from "../components/AppShell";
import AdminNav from "../components/AdminNav";
import { Field, Alert } from "../components/Field";
import * as patientApi from "../api/patientApi";
import { parseApiError } from "../utils/parseApiError";
import "../components/Form.css";
import "./Admin.css";

const EMPTY = { firstName: "", lastName: "", email: "", phoneNumber: "", dateOfBirth: "", allergies: "" };

export default function AddPatient() {
    const navigate = useNavigate();
    const [form, setForm] = useState(EMPTY);
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    function update(field, value) {
        setForm((prev) => ({ ...prev, [field]: value }));
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setError("");
        setSubmitting(true);
        try {
            const allergies = form.allergies
                .split(",")
                .map((a) => a.trim())
                .filter(Boolean);
            await patientApi.createPatient({ ...form, allergies });
            navigate("/admin/patients", { replace: true, state: { justAdded: true } });
        } catch (err) {
            setError(parseApiError(err).message);
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <AppShell>
            <h1>Add a patient</h1>
            <p className="appts-sub" style={{ marginBottom: "20px" }}>
                For walk-ins who don't have a login account of their own yet.
            </p>

            <AdminNav />

            <div className="consult-card" style={{ marginTop: "24px" }}>
                <form onSubmit={handleSubmit} noValidate>
                    <Alert type="error">{error}</Alert>

                    <div className="field-row">
                        <Field
                            id="firstName"
                            label="First name"
                            value={form.firstName}
                            onChange={(e) => update("firstName", e.target.value)}
                            required
                        />
                        <Field
                            id="lastName"
                            label="Last name"
                            value={form.lastName}
                            onChange={(e) => update("lastName", e.target.value)}
                            required
                        />
                    </div>

                    <Field
                        id="email"
                        label="Email"
                        type="email"
                        value={form.email}
                        onChange={(e) => update("email", e.target.value)}
                        required
                    />

                    <div className="field-row">
                        <Field
                            id="phoneNumber"
                            label="Phone number"
                            value={form.phoneNumber}
                            onChange={(e) => update("phoneNumber", e.target.value)}
                            placeholder="e.g. 0821234567"
                            required
                        />
                        <Field
                            id="dateOfBirth"
                            label="Date of birth"
                            type="date"
                            value={form.dateOfBirth}
                            onChange={(e) => update("dateOfBirth", e.target.value)}
                            required
                        />
                    </div>

                    <Field
                        id="allergies"
                        label="Allergies (optional)"
                        value={form.allergies}
                        onChange={(e) => update("allergies", e.target.value)}
                        placeholder="Comma-separated, e.g. penicillin, latex"
                    />

                    <button className="btn-primary" type="submit" disabled={submitting}>
                        {submitting ? "Adding…" : "Add patient"}
                    </button>
                </form>
            </div>
        </AppShell>
    );
}
