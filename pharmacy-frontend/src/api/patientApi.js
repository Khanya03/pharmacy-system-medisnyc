frontend>api
patientApi.js
import client from "./client";

export function getAllPatients() {
  return client.get("/api/patients").then((res) => res.data);
}

export function getPatientById(medicalId) {
  return client.get(/api/patients/${medicalId}).then((res) => res.data);
}

export function createPatient({ firstName, lastName, email, phoneNumber, dateOfBirth, allergies }) {
  return client
    .post("/api/patients", { firstName, lastName, email, phoneNumber, dateOfBirth, allergies })
    .then((res) => res.data);
}