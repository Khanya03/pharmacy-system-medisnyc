import client from "./client";

export function getAllDoctors() {
    return client.get("/api/doctors").then((res) => res.data);
}

export function getDoctorById(doctorId) {
    return client.get(`/api/doctors/${doctorId}`).then((res) => res.data);
}

export function createDoctor({ firstName, lastName, specialization, phoneNumber, email }) {
    return client
        .post("/api/doctors", { firstName, lastName, specialization, phoneNumber, email })
        .then((res) => res.data);
}

export function updateDoctor(doctorId, { firstName, lastName, specialization, phoneNumber, email }) {
    return client
        .put(`/api/doctors/${doctorId}`, { firstName, lastName, specialization, phoneNumber, email })
        .then((res) => res.data);
}

export function deleteDoctor(doctorId) {
    return client.delete(`/api/doctors/${doctorId}`);
}
