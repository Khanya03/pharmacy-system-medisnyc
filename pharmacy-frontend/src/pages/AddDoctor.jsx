import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import AppShell from "../components/AppShell";
import AdminNav from "../components/AdminNav";
import { Field, SelectField, Alert } from "../components/Field";
import * as doctorApi from "../api/doctorApi";
import { parseApiError } from "../utils/parseApiError";
import "../components/Form.css";
import "./Admin.css";

const EMPTY = { firstName: "", lastName: "", specialization: "", phoneNumber: "", email: "" };

export default function AddDoctor() {
    const navigate = useNavigate();
    const [form, setForm] = useState(EMPTY);
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [specializations, setSpecializations] = useState([]);
    const [loadingSpecializations, setLoadingSpecializations] = useState(true);

    useEffect(() => {
        doctorApi.getSpecializations()
            .then(setSpecializations)
            .catch((err) => setError(parseApiError(err).message))
            .finally(() => setLoadingSpecializations(false));
    }, []);

    function update(field, value) {
        setForm((prev) => ({ ...prev, [field]: value }));
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setError("");
        setSubmitting(true);
        try {
            await doctorApi.createDoctor(form);
            navigate("/admin/doctors", { replace: true, state: { justAdded: true } });
        } catch (err) {
            setError(parseApiError(err).message);
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <AppShell>
            <h1>Add a doctor</h1>
            <p className="appts-sub" style={{ marginBottom: "20px" }}>
                Doctors need to exist here before a patient can book an appointment with them.
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

                    <SelectField
                        id="specialization"
                        label="Specialization"
                        value={form.specialization}
                        onChange={(e) => update("specialization", e.target.value)}
                        disabled={loadingSpecializations}
                        required
                    >
                        <option value="">Choose a specialization</option>
                        {specializations.map((specialization) => (
                            <option key={specialization} value={specialization}>{specialization}</option>
                        ))}
                    </SelectField>

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
                            id="email"
                            label="Email"
                            type="email"
                            value={form.email}
                            onChange={(e) => update("email", e.target.value)}
                            required
                        />
                    </div>

                    <button className="btn-primary" type="submit" disabled={submitting || loadingSpecializations || !form.specialization}>
                        {submitting ? "Adding…" : "Add doctor"}
                    </button>
                </form>
            </div>
        </AppShell>
    );
}
