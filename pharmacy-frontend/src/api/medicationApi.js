
import client from "./client";

// Maps onto MedicationController (/api/medications).

export function getAllMedications() {
    return client.get("/api/medications").then((res) => res.data);
}

export function getMedication(medicationId) {
    return client.get(`/api/medications/${medicationId}`).then((res) => res.data);
}

export function searchMedications(name) {
    return client.get("/api/medications/search", { params: { name } }).then((res) => res.data);
}

export function addMedication({ medicationName, dosageForm, manufacturer, strength, description, category }) {
    return client
        .post("/api/medications", { medicationName, dosageForm, manufacturer, strength, description, category })
        .then((res) => res.data);
}

export function updateMedication(medicationId, medication) {
    return client.put(`/api/medications/${medicationId}`, medication).then((res) => res.data);
}

export function deleteMedication(medicationId) {
    return client.delete(`/api/medications/${medicationId}`);
}