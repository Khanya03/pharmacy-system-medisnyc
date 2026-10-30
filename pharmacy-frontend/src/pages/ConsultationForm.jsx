
import { useEffect, useState } from "react";
import { useSearchParams, Link } from "react-router-dom";
import AppShell from "../components/AppShell";
import { Field, SelectField, Alert } from "../components/Field";
import { useAuth } from "../context/useAuth";
import * as consultationApi from "../api/consultationApi";
import * as medicationApi from "../api/medicationApi";
import * as appointmentApi from "../api/appointmentApi";
import { getPatientDetails, formatPatientName } from "../utils/patientLookup";
import { parseApiError } from "../utils/parseApiError";
import "../components/Form.css";
import "./Consultations.css";

function emptyItem() {
    return { medicationId: "", dosage: "", frequency: "", durationDays: "", quantity: "" };
}

export default function ConsultationForm() {
    const { profileId } = useAuth();
    const [params] = useSearchParams();
    const patientId = params.get("patientId") || "";
    const appointmentId = params.get("appointmentId") || "";
    const doctorId = profileId();

    const [record, setRecord] = useState({ diagnosis: "", consultationNotes: "", treatmentPlan: "" });
    const [recordSaved, setRecordSaved] = useState(false);
    const [savingRecord, setSavingRecord] = useState(false);
    const [recordError, setRecordError] = useState("");
    const [appointmentError, setAppointmentError] = useState("");
    const [appointmentRetrying, setAppointmentRetrying] = useState(false);
    const [patientDetails, setPatientDetails] = useState(null);
    const [patientAppointments, setPatientAppointments] = useState([]);
    const [selectedAppointmentId, setSelectedAppointmentId] = useState(appointmentId);
    const [appointmentListError, setAppointmentListError] = useState("");

    const [medications, setMedications] = useState([]);
    const [rxItems, setRxItems] = useState([emptyItem()]);
    const [rxDetails, setRxDetails] = useState({ expiryDate: "", instructions: "", refillsAllowed: "0" });
    const [savingRx, setSavingRx] = useState(false);
    const [rxError, setRxError] = useState("");
    const [rxSaved, setRxSaved] = useState(false);
    const [medicationLoadError, setMedicationLoadError] = useState("");

    useEffect(() => {
        medicationApi.getAllMedications()
            .then((list) => {
                setMedications(list);
                setMedicationLoadError("");
            })
            .catch((err) => {
                setMedications([]);
                setMedicationLoadError(parseApiError(err).message || "Could not load the medication list. Try refreshing the page.");
            });
    }, []);

    useEffect(() => {
        let active = true;
        getPatientDetails(patientId).then((patient) => {
            if (active) setPatientDetails(patient);
        });
        return () => {
            active = false;
        };
    }, [patientId]);

    useEffect(() => {
        if (appointmentId || !patientId) return undefined;
        let active = true;
        appointmentApi.getAppointmentsByDoctor(doctorId)
            .then((appointments) => {
                if (!active) return;
                setPatientAppointments(appointments.filter((appointment) =>
                    appointment.patientId === patientId &&
                    ["SCHEDULED", "CONFIRMED"].includes(appointment.status)));
            })
            .catch((err) => {
                if (active) setAppointmentListError(parseApiError(err).message);
            });
        return () => {
            active = false;
        };
    }, [appointmentId, patientId, doctorId]);

    async function completeAppointment(targetAppointmentId) {
        if (!targetAppointmentId) return;
        setAppointmentError("");
        setAppointmentRetrying(true);
        try {
            const appointment = await appointmentApi.getAppointmentById(targetAppointmentId);
            if (appointment.status === "SCHEDULED") {
                await appointmentApi.updateAppointmentStatus(targetAppointmentId, "CONFIRMED");
                await appointmentApi.updateAppointmentStatus(targetAppointmentId, "COMPLETED");
            } else if (appointment.status === "CONFIRMED") {
                await appointmentApi.updateAppointmentStatus(targetAppointmentId, "COMPLETED");
            } else if (appointment.status !== "COMPLETED") {
                setAppointmentError("This appointment cannot be marked completed in its current status.");
                return;
            }
        } catch (err) {
            setAppointmentError(parseApiError(err).message);
        } finally {
            setAppointmentRetrying(false);
        }
    }

    function updateRecord(field, value) {
        setRecord((prev) => ({ ...prev, [field]: value }));
    }

    async function handleSaveRecord(e) {
        e.preventDefault();
        setRecordError("");
        setSavingRecord(true);
        try {
            await consultationApi.createMedicalRecord({
                patientId,
                appointmentId: selectedAppointmentId || null,
                ...record,
            });
            setRecordSaved(true);
            if (selectedAppointmentId) await completeAppointment(selectedAppointmentId);
        } catch (err) {
            setRecordError(parseApiError(err).message);
        } finally {
            setSavingRecord(false);
        }
    }

    function updateRxDetail(field, value) {
        setRxDetails((prev) => ({ ...prev, [field]: value }));
    }

    function updateItem(index, field, value) {
        setRxItems((prev) => prev.map((it, i) => (i === index ? { ...it, [field]: value } : it)));
    }

    function addItem() {
        setRxItems((prev) => [...prev, emptyItem()]);
    }

    function removeItem(index) {
        setRxItems((prev) => prev.filter((_, i) => i !== index));
    }

    async function handleSavePrescription(e) {
        e.preventDefault();
        setRxError("");

        const items = rxItems
            .filter((it) => it.medicationId)
            .map((it) => ({
                medicationId: it.medicationId,
                dosage: it.dosage,
                frequency: it.frequency,
                durationDays: Number(it.durationDays) || 0,
                quantity: Number(it.quantity) || 0,
            }));

        if (items.length === 0) {
            setRxError("Add at least one medication.");
            return;
        }

        setSavingRx(true);
        try {
            await consultationApi.createPrescription({
                patientId,
                expiryDate: rxDetails.expiryDate,
                instructions: rxDetails.instructions,
                refillsAllowed: rxDetails.refillsAllowed,
                items,
            });
            setRxSaved(true);
        } catch (err) {
            setRxError(parseApiError(err).message);
        } finally {
            setSavingRx(false);
        }
    }

    const appointmentDateLabel = (date) => new Date(date).toLocaleString();

    if (rxSaved) {
        return (
            <AppShell>
                <div className="consult-card consult-done">
                    <div className="consult-done-mark">✓</div>
                    <h2>Consultation complete</h2>
                    <p className="consult-card-sub">
                        Medical record and prescription have been saved for {formatPatientName(patientDetails, patientId)}.
                    </p>
                    <Link to="/consultations" className="btn-primary" style={{ display: "inline-block", width: "auto", padding: "10px 22px", textDecoration: "none" }}>
                        Back to today's appointments
                    </Link>
                </div>
            </AppShell>
        );
    }

    return (
        <AppShell>
            <h1>Consultation</h1>
            <div className="consult-context">
                <span>PATIENT: {formatPatientName(patientDetails, patientId || "—")}</span>
                {selectedAppointmentId && <span>APPOINTMENT: {selectedAppointmentId}</span>}
            </div>

            <div className="consult-card">
                <h2>1. Medical record</h2>
                <p className="consult-card-sub">Diagnosis, notes, and the treatment plan for this visit.</p>
                {!appointmentId && !recordSaved && (
                    <>
                        <SelectField
                            id="appointmentId"
                            label="Link to an appointment (optional)"
                            value={selectedAppointmentId}
                            onChange={(event) => setSelectedAppointmentId(event.target.value)}
                        >
                            <option value="">No appointment</option>
                            {patientAppointments.map((appointment) => (
                                <option key={appointment.appointmentId} value={appointment.appointmentId}>
                                    {appointmentDateLabel(appointment.appointmentDate)} — {appointment.status}
                                </option>
                            ))}
                        </SelectField>
                        <Alert type="error">{appointmentListError}</Alert>
                    </>
                )}
                <form onSubmit={handleSaveRecord} noValidate>
                    <Alert type="error">{recordError}</Alert>
                    {recordSaved && <Alert type="success">Medical record saved.</Alert>}
                    {appointmentError && (
                        <div className="alert alert-warning" role="alert">
                            <p>{appointmentError} Your medical record is saved.</p>
                            <button
                                type="button"
                                className="btn-secondary appointment-retry"
                                disabled={appointmentRetrying}
                                onClick={() => completeAppointment(selectedAppointmentId)}
                            >
                                {appointmentRetrying ? "Updating…" : "Mark appointment completed"}
                            </button>
                        </div>
                    )}

                    <Field
                        id="diagnosis"
                        label="Diagnosis"
                        value={record.diagnosis}
                        onChange={(e) => updateRecord("diagnosis", e.target.value)}
                        disabled={recordSaved}
                        required
                    />
                    <div className="field">
                        <label className="field-label" htmlFor="consultationNotes">
                            Consultation notes
                        </label>
                        <textarea
                            id="consultationNotes"
                            className="field-input"
                            value={record.consultationNotes}
                            onChange={(e) => updateRecord("consultationNotes", e.target.value)}
                            disabled={recordSaved}
                        />
                    </div>
                    <div className="field">
                        <label className="field-label" htmlFor="treatmentPlan">
                            Treatment plan
                        </label>
                        <textarea
                            id="treatmentPlan"
                            className="field-input"
                            value={record.treatmentPlan}
                            onChange={(e) => updateRecord("treatmentPlan", e.target.value)}
                            disabled={recordSaved}
                        />
                    </div>

                    {!recordSaved && (
                        <button className="btn-primary" type="submit" disabled={savingRecord}>
                            {savingRecord ? "Saving…" : "Save medical record"}
                        </button>
                    )}
                </form>
            </div>

            {recordSaved && (
                <div className="consult-card">
                    <h2>2. Prescription (optional)</h2>
                    <p className="consult-card-sub">Add medications if this patient needs a prescription.</p>
                    <form onSubmit={handleSavePrescription} noValidate>
                        <Alert type="error">{medicationLoadError || rxError}</Alert>

                        {rxItems.map((item, i) => (
                            <div className="rx-item-row" key={i}>
                                <SelectField
                                    label="Medication"
                                    value={item.medicationId}
                                    onChange={(e) => updateItem(i, "medicationId", e.target.value)}
                                >
                                    <option value="">Select…</option>
                                    {medications.map((m) => (
                                        <option key={m.medicationId} value={m.medicationId}>
                                            {m.medicationName} {m.strength ? `(${m.strength})` : ""}
                                        </option>
                                    ))}
                                </SelectField>
                                <Field
                                    label="Dosage"
                                    value={item.dosage}
                                    onChange={(e) => updateItem(i, "dosage", e.target.value)}
                                    placeholder="e.g. 500mg"
                                />
                                <Field
                                    label="Frequency"
                                    value={item.frequency}
                                    onChange={(e) => updateItem(i, "frequency", e.target.value)}
                                    placeholder="e.g. 2x/day"
                                />
                                <Field
                                    label="Qty"
                                    type="number"
                                    min="0"
                                    value={item.quantity}
                                    onChange={(e) => updateItem(i, "quantity", e.target.value)}
                                />
                                {rxItems.length > 1 && (
                                    <button type="button" className="rx-item-remove" onClick={() => removeItem(i)} aria-label="Remove">
                                        ×
                                    </button>
                                )}
                            </div>
                        ))}

                        <button type="button" className="rx-add-item-btn" onClick={addItem}>
                            + Add another medication
                        </button>

                        <div className="field-row">
                            <Field
                                id="expiryDate"
                                label="Expiry date"
                                type="date"
                                value={rxDetails.expiryDate}
                                onChange={(e) => updateRxDetail("expiryDate", e.target.value)}
                            />
                            <Field
                                id="refillsAllowed"
                                label="Refills allowed"
                                type="number"
                                min="0"
                                value={rxDetails.refillsAllowed}
                                onChange={(e) => updateRxDetail("refillsAllowed", e.target.value)}
                            />
                        </div>

                        <Field
                            id="instructions"
                            label="Instructions"
                            value={rxDetails.instructions}
                            onChange={(e) => updateRxDetail("instructions", e.target.value)}
                            placeholder="e.g. Take with food"
                        />

                        <button className="btn-primary" type="submit" disabled={savingRx}>
                            {savingRx ? "Saving…" : "Issue prescription"}
                        </button>
                    </form>
                </div>
            )}

            {recordSaved && (
                <Link to="/consultations" style={{ fontSize: "13.5px", fontWeight: 600 }}>
                    Skip prescription, back to today's appointments
                </Link>
            )}
        </AppShell>
    );
}