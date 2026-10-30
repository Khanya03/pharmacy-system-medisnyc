
import client from "./client";


export function getMine() {
    return client.get("/api/notifications/me").then((res) => res.data);
}

export function markAsRead(notificationId) {
    return client.put(`/api/notifications/${notificationId}/read`).then((res) => res.data);
}