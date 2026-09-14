import client from "./client";

// Module 1 — Authentication & User Management
// Maps 1:1 onto AuthController (/api/auth) and ProfileController (/api/profile).

export function register({ username, email, password, firstName, lastName, role, linkedProfileId }) {
    return client
        .post("/api/auth/register", {
            username,
            email,
            password,
            firstName,
            lastName,
            role,
            linkedProfileId: linkedProfileId || null,
        })
        .then((res) => res.data);
}

export function login({ username, password }) {
    return client.post("/api/auth/login", { username, password }).then((res) => res.data);
}

export function forgotPassword({ email }) {
    return client.post("/api/auth/forgot-password", { email }).then((res) => res.data);
}

export function resetPassword({ token, newPassword }) {
    return client.post("/api/auth/reset-password", { token, newPassword }).then((res) => res.data);
}

export function getMyProfile() {
    return client.get("/api/profile/me").then((res) => res.data);
}

export function updateMyProfile({ firstName, lastName, email }) {
    const params = new URLSearchParams();
    if (firstName) params.append("firstName", firstName);
    if (lastName) params.append("lastName", lastName);
    if (email) params.append("email", email);
    return client.put(/api/profile/me?${params.toString()}).then((res) => res.data);
}