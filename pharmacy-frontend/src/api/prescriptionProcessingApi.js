import client from "./client";

// Module 5 — Prescription Processing Module.
// Maps onto PrescriptionProcessingController (/api/prescription-processing).

export function getQueue() {
    return client.get("/api/prescription-processing/queue").then((res) => res.data);
}

export function getReadyForCollection() {
    return client.get("/api/prescription-processing/ready-for-collection").then((res) => res.data);
}

export function getByPatient(patientId) {
    return client.get(/api/prescription-processing/patient/${patientId}).then((res) => res.data);
}

export function getByDoctor(doctorId) {
    return client.get(/api/prescription-processing/doctor/${doctorId}).then((res) => res.data);
}

export function trackPrescription(prescriptionId) {
    return client.get(/api/prescription-processing/${prescriptionId}).then((res) => res.data);
}

export function advanceStatus(prescriptionId) {
    return client.put(/api/prescription-processing/${prescriptionId}/advance).then((res) => res.data);
}