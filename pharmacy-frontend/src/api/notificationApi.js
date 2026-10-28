
import client from "./client";

// Module 5 — Notify Patient API. Patient identity is resolved by the backend.

export function getMine() {
    return client.get("/api/notifications/me").then((res) => res.data);
}

export function markAsRead(notificationId) {
    return client.put(`/api/notifications/${notificationId}/read`).then((res) => res.data);
}