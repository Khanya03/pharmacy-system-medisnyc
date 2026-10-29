import client from "./client";

// Module 6 — Reports & Administration. Maps onto AdminController (/api/admin) and
// ReportController (/api/reports).

export function getAllUsers() {
    return client.get("/api/admin/users").then((res) => res.data);
}

export function setUserEnabled(userId, enabled) {
    return client.put(`/api/admin/users/${userId}/enabled`, null, { params: { enabled } }).then((res) => res.data);
}

export function assignRole(userId, role) {
    return client.put(`/api/admin/users/${userId}/roles`, null, { params: { role } }).then((res) => res.data);
}

export function setLinkedProfileId(userId, linkedProfileId) {
    return client
        .put(`/api/admin/users/${userId}/linked-profile`, null, { params: { linkedProfileId } })
        .then((res) => res.data);
}

export function linkNewPatientProfile(userId, { phoneNumber, dateOfBirth }) {
    return client
        .put(`/api/admin/users/${userId}/link-patient`, { phoneNumber, dateOfBirth })
        .then((res) => res.data);
}

export function linkExistingDoctorProfile(userId, doctorId) {
    return client
        .put(`/api/admin/users/${userId}/link-doctor`, null, { params: { doctorId } })
        .then((res) => res.data);
}

export function deleteUser(userId, hard = false) {
    return client.delete(`/api/admin/users/${userId}`, { params: { hard } });
}

export function createDoctorLogin({ doctorId, username, email, password }) {
    return client.post("/api/admin/users/doctor-login", { doctorId, username, email, password }).then((res) => res.data);
}

export function getAuditLogs({ page = 0, size = 25, username = "", action = "" } = {}) {
    return client.get("/api/admin/audit-logs", {
        params: { page, size, username: username || undefined, action: action || undefined },
    }).then((res) => res.data);
}

export function getDashboardStatistics() {
    return client.get("/api/reports/dashboard-statistics").then((res) => res.data);
}

export function getRevenueReport({ from, to } = {}) {
    return client.get("/api/reports/revenue", {
        params: { from: from || undefined, to: to || undefined },
    }).then((res) => res.data);
}