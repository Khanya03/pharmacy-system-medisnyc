
import client from "./client";

// Module 4/6 — Order fulfilment. Maps onto OrderController (/api/orders).

export function getAllOrders() {
  return client.get("/api/orders").then((res) => res.data);
}

export function getOrdersByPatient(patientId) {
  return client.get(`/api/orders/patient/${patientId}`).then((res) => res.data);
}

export function createOrder({ patientId, prescriptionId, pharmacistId, notes, items }) {
  return client
    .post("/api/orders", {
      patientId,
      prescriptionId: prescriptionId || null,
      pharmacistId: pharmacistId || null,
      notes,
      items,
    })
    .then((res) => res.data);
}

export function updateOrderStatus(orderId, status) {
  return client.put(`/api/orders/${orderId}/status`, { status }).then((res) => res.data);
}