appointmentApi.js
import client from "./client";

// Module 2 — Patient & Appointment Module.
// Maps 1:1 onto AppointmentController (/api/appointments).

export function bookAppointment({ patientId, doctorId, appointmentDate, reason, notes }) {
  return client
    .post("/api/appointments", { patientId, doctorId, appointmentDate, reason, notes })
    .then((res) => res.data);
}

export function getAppointmentsByPatient(patientId) {
  return client.get(`/api/appointments/patient/${patientId}`).then((res) => res.data);
}

export function getAppointmentsByDoctor(doctorId) {
  return client.get(`/api/appointments/doctor/${doctorId}`).then((res) => res.data);
}

export function getAllAppointments() {
  return client.get("/api/appointments").then((res) => res.data);
}

export function getAppointmentById(appointmentId) {
  return client.get(`/api/appointments/${appointmentId}`).then((res) => res.data);
}

export function updateAppointmentStatus(appointmentId, status) {
  return client
    .put(`/api/appointments/${appointmentId}/status`, null, { params: { status } })
    .then((res) => res.data);
}

export function cancelAppointment(appointmentId) {
  return client.put(`/api/appointments/${appointmentId}/cancel`).then((res) => res.data);
}