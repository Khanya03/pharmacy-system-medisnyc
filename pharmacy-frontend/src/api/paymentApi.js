import client from "./client";

export function getMyPayments() {
  return client.get("/api/payments/my").then((response) => response.data);
}

export function getPaymentsForOrder(orderId) {
  return client.get(`/api/payments/order/${encodeURIComponent(orderId)}`).then((response) => response.data);
}

export function getReceipt(paymentId) {
  return client.get(`/api/payments/${encodeURIComponent(paymentId)}/receipt`).then((response) => response.data);
}

export function payByCard(payment) {
  return client.post("/api/payments/card", payment).then((response) => response.data);
}

export function refundPayment(paymentId, reason) {
  return client.post(`/api/payments/${encodeURIComponent(paymentId)}/refund`, { reason }).then((response) => response.data);
}

export function getPayments({ from, to, status, page = 0, size = 10 } = {}) {
  return client.get("/api/payments", {
    params: { from: from || undefined, to: to || undefined, status: status || undefined, page, size },
  }).then((response) => response.data);
}

export function exportPayments({ from, to } = {}) {
  return client.get("/api/reports/payments/export", {
    params: { from: from || undefined, to: to || undefined },
    responseType: "blob",
  }).then((response) => response.data);
}
