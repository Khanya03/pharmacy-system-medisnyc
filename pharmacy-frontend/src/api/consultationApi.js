import client from "./client";

// Maps onto ConsultationController (/api/medical-records, /api/consultations).

export function createMedicalRecord({ patientId, appointmentId, diagnosis, consultationNotes, treatmentPlan }) {
    return client
        .post("/api/medical-records", {
            patientId,
            appointmentId,
            visitDate: new Date().toISOString().slice(0, 19),
            diagnosis,
            consultationNotes,
            treatmentPlan,
        })
        .then((res) => res.data);
}

export function getRecordsByPatient(patientId) {
    return client.get(`/api/medical-records/patient/${patientId}`).then((res) => res.data);
}

export function getRecordsByDoctor(doctorId) {
    return client.get(`/api/medical-records/doctor/${doctorId}`).then((res) => res.data);
}

export function createPrescription({ patientId, expiryDate, instructions, refillsAllowed, items }) {
    const prescription = {
        patientId,
        dateIssued: new Date().toISOString().slice(0, 10),
        expiryDate,
        instructions,
        refillsAllowed: Number(refillsAllowed) || 0,
        refillsUsed: 0,
        status: "ACTIVE",
    };
    return client.post("/api/consultations/prescriptions", { prescription, items }).then((res) => res.data);
}

export function getPrescriptionItems(prescriptionId) {
    return client.get(`/api/consultations/prescriptions/${prescriptionId}/items`).then((res) => res.data);
}
