import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import AppShell from "../components/AppShell";
import { Alert, Field, SelectField } from "../components/Field";
import { useAuth } from "../context/useAuth";
import * as appointmentApi from "../api/appointmentApi";
import * as doctorApi from "../api/doctorApi";
import symptomSpecialties from "../config/symptomSpecialties";
import { parseApiError } from "../utils/parseApiError";
import "../components/Form.css";
import "./Appointments.css";

const SHOW_ALL = "all";

function formatDateTime(date) {
    const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return localDate.toISOString().slice(0, 16);
}

export default function BookAppointment() {
    const { profileId } = useAuth();
    const navigate = useNavigate();

    const [specializations, setSpecializations] = useState([]);
    const [specialization, setSpecialization] = useState("");
    const [symptomChoice, setSymptomChoice] = useState(SHOW_ALL);
    const [doctors, setDoctors] = useState([]);
    const [loadedSpecialization, setLoadedSpecialization] = useState(null);
    const [loadingSpecializations, setLoadingSpecializations] = useState(true);
    const [doctorsError, setDoctorsError] = useState("");
    const [clock, setClock] = useState(() => new Date());
    const [form, setForm] = useState({
        doctorId: "",
        appointmentDate: "",
        reason: "",
        notes: "",
    });
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const minDateTime = useMemo(() => formatDateTime(clock), [clock]);

    useEffect(() => {
        doctorApi.getSpecializations()
            .then(setSpecializations)
            .catch((err) => setDoctorsError(parseApiError(err).message))
            .finally(() => setLoadingSpecializations(false));
    }, []);

    useEffect(() => {
        let cancelled = false;
        doctorApi.getDoctors(specialization || undefined)
            .then((result) => {
                if (!cancelled) setDoctors(result);
            })
            .catch((err) => {
                if (!cancelled) setDoctorsError(parseApiError(err).message);
            })
            .finally(() => {
                if (!cancelled) setLoadedSpecialization(specialization);
            });
        return () => {
            cancelled = true;
        };
    }, [specialization]);

    function selectSymptom(value) {
        setDoctorsError("");
        setSymptomChoice(value);
        const selectedSymptom = symptomSpecialties.find((item) => item.label === value);
        const nextSpecialization = selectedSymptom?.specialty || "";
        setSpecialization(nextSpecialization);
        setForm((previous) => ({
            ...previous,
            doctorId: "",
            reason: selectedSymptom?.label || "",
        }));
    }

    function selectSpecialization(value) {
        setDoctorsError("");
        setSpecialization(value);
        const matchingSymptom = symptomSpecialties.find((item) => item.specialty === value);
        setSymptomChoice(matchingSymptom?.label || SHOW_ALL);
        setForm((previous) => ({
            ...previous,
            doctorId: "",
            reason: matchingSymptom?.label || "",
        }));
    }

    function update(field, value) {
        setForm((previous) => ({ ...previous, [field]: value }));
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setError("");

        if (!form.doctorId) {
            setError("Choose a doctor to book with.");
            return;
        }
        if (!form.appointmentDate) {
            setError("Choose a date and time.");
            return;
        }
        if (new Date(form.appointmentDate) <= new Date()) {
            setError("Choose a date and time in the future.");
            return;
        }

        setSubmitting(true);
        try {
            await appointmentApi.bookAppointment({
                patientId: profileId(),
                doctorId: form.doctorId,
                appointmentDate: form.appointmentDate,
                reason: form.reason,
                notes: form.notes,
            });
            navigate("/appointments", { replace: true });
        } catch (err) {
            setError(parseApiError(err).message);
        } finally {
            setSubmitting(false);
        }
    }

    const selectedDoctor = doctors.find((doctor) => doctor.doctorId === form.doctorId);
    const loadingDoctors = loadedSpecialization !== specialization;

    return (
        <AppShell>
            <h1>Book an appointment</h1>
            <p className="appts-sub">Find the right doctor, then choose a time that works for you.</p>

            <div className="book-card booking-card" style={{ marginTop: "24px" }}>
                <form onSubmit={handleSubmit} noValidate>
                    <Alert type="error">{error || doctorsError}</Alert>

                    <section className="booking-step">
                        <h2>1. What do you need help with?</h2>
                        <SelectField
                            id="symptom"
                            label="Choose a concern"
                            value={symptomChoice}
                            onChange={(event) => selectSymptom(event.target.value)}
                        >
                            <option value={SHOW_ALL}>I'm not sure / show all doctors</option>
                            {symptomSpecialties.map((item) => (
                                <option key={item.label} value={item.label}>{item.label}</option>
                            ))}
                        </SelectField>
                        <SelectField
                            id="specialization"
                            label="Specialty"
                            value={specialization}
                            disabled={loadingSpecializations}
                            onChange={(event) => selectSpecialization(event.target.value)}
                        >
                            <option value="">All specialties</option>
                            {specializations.map((item) => (
                                <option key={item} value={item}>{item}</option>
                            ))}
                        </SelectField>
                        <p className="booking-helper-note">
                            This helps you find a suitable doctor; it is not a diagnosis.
                        </p>
                        <p className="booking-emergency-note">
                            In an emergency call 10177 or 112 — do not book an appointment.
                        </p>
                    </section>

                    <section className="booking-step">
                        <h2>2. Choose a doctor</h2>
                        {!loadingDoctors && !doctorsError && (
                            <p className="doctor-count">
                                {doctors.length} {doctors.length === 1 ? "doctor" : "doctors"} available
                            </p>
                        )}
                        {loadingDoctors ? (
                            <p className="doctor-empty" role="status">Loading doctors…</p>
                        ) : doctorsError ? null : doctors.length === 0 ? (
                            <div className="doctor-empty">
                                <p>
                                    {specialization
                                        ? "No doctors available for this specialty yet. Try another specialty or view all doctors."
                                        : "No doctors are available yet. Please check again later."}
                                </p>
                                {specialization && (
                                    <button
                                        className="btn-secondary show-all-doctors"
                                        type="button"
                                        onClick={() => selectSpecialization("")}
                                    >
                                        Show all doctors
                                    </button>
                                )}
                            </div>
                        ) : (
                            <div className="doctor-picker">
                                {doctors.map((doctor) => (
                                    <label
                                        key={doctor.doctorId}
                                        className={`doctor-option ${form.doctorId === doctor.doctorId ? "selected" : ""}`}
                                    >
                                        <span>
                                            <span className="doctor-option-name">
                                                Dr. {doctor.firstName} {doctor.lastName}
                                            </span>
                                            <span className="doctor-option-spec">{doctor.specialization}</span>
                                        </span>
                                        <input
                                            className="doctor-option-radio"
                                            type="radio"
                                            name="doctorId"
                                            value={doctor.doctorId}
                                            checked={form.doctorId === doctor.doctorId}
                                            onChange={() => update("doctorId", doctor.doctorId)}
                                            aria-label={`Choose Dr. ${doctor.firstName} ${doctor.lastName}`}
                                        />
                                    </label>
                                ))}
                            </div>
                        )}
                    </section>

                    <section className="booking-step">
                        <h2>3. Choose a date and time</h2>
                        {selectedDoctor && (
                            <p className="selected-doctor-note">
                                Booking with Dr. {selectedDoctor.firstName} {selectedDoctor.lastName}
                            </p>
                        )}
                        <Field
                            id="appointmentDate"
                            label="Date & time"
                            type="datetime-local"
                            min={minDateTime}
                            value={form.appointmentDate}
                            onFocus={() => setClock(new Date())}
                            onChange={(event) => update("appointmentDate", event.target.value)}
                            required
                        />
                        <Field
                            id="reason"
                            label="Reason for visit"
                            value={form.reason}
                            onChange={(event) => update("reason", event.target.value)}
                            placeholder="Briefly describe your reason for visiting"
                        />
                        <Field
                            id="notes"
                            label="Notes (optional)"
                            value={form.notes}
                            onChange={(event) => update("notes", event.target.value)}
                            placeholder="Anything else you would like the doctor to know"
                        />
                        <button
                            className="btn-primary"
                            type="submit"
                            disabled={submitting || !form.doctorId || !form.appointmentDate}
                        >
                            {submitting ? "Booking…" : "Book appointment"}
                        </button>
                    </section>
                </form>
            </div>
        </AppShell>
    );
}
