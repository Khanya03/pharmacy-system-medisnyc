import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AppShell from "../components/AppShell";
import StatusBadge from "../components/StatusBadge";
import * as orderApi from "../api/orderApi";
import * as paymentApi from "../api/paymentApi";
import { parseApiError } from "../utils/parseApiError";
import { Alert } from "../components/Field";
import "./Payments.css";

function orderDate(value) {
  return value ? new Date(value).toLocaleString() : "—";
}

export default function MyOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [loadError, setLoadError] = useState(false);

  const load = useCallback(async () => {
    try {
      const data = await orderApi.getMyOrders();
      setOrders([...data].sort((a, b) => new Date(b.orderDate || 0) - new Date(a.orderDate || 0)));
      setError("");
      setLoadError(false);
    } catch (requestError) {
      setError(parseApiError(requestError).message);
      setLoadError(true);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { queueMicrotask(load); }, [load]);

  async function openReceipt(order) {
    try {
      const payments = await paymentApi.getPaymentsForOrder(order.orderId);
      const paid = payments.find((payment) => payment.status === "PAID" || payment.status === "REFUNDED");
      if (!paid) throw new Error("No receipt is available for this order.");
      window.location.assign(`/receipts/${encodeURIComponent(paid.paymentId)}`);
    } catch (requestError) {
      setError(requestError.response ? parseApiError(requestError).message : requestError.message);
    }
  }

  return (
    <AppShell>
      <p className="appts-eyebrow">Patient account</p>
      <h1>My orders &amp; payments</h1>
      <p className="appts-sub">Review your orders, pay outstanding balances, and open paid receipts.</p>
      <Alert>{error}</Alert>
      {loading ? <p className="appts-loading">Loading your orders…</p>
        : loadError ? <div className="appts-empty"><p>Your orders could not be loaded.</p>
          <button className="admin-toggle-btn" type="button" onClick={() => { setLoading(true); load(); }}>Retry</button>
        </div>
          : orders.length === 0 ? <div className="appts-empty"><p>You don’t have any orders yet.</p></div>
          : <div className="my-orders-list">
            {orders.map((order) => {
              const paymentStatus = order.paymentStatus || "UNPAID";
              return (
                <article className="my-order-card" key={order.orderId}>
                  <div className="my-order-main">
                    <div className="my-order-heading">
                      <strong>{order.orderId}</strong>
                      <span>{orderDate(order.orderDate)}</span>
                    </div>
                    <ul>{(order.items || []).map((item) => (
                      <li key={item.orderItemId}>{item.medicationName || item.medicine || item.medicationId} × {item.quantity}</li>
                    ))}</ul>
                    <div className="my-order-badges">
                      <StatusBadge status={order.dispensingStatus} />
                      <StatusBadge status={paymentStatus} />
                    </div>
                  </div>
                  <div className="my-order-action">
                    <strong>R{Number(order.totalAmount || 0).toFixed(2)}</strong>
                    {paymentStatus === "UNPAID"
                      ? <Link className="btn-primary" to={`/orders/${encodeURIComponent(order.orderId)}/pay`}>Pay now</Link>
                      : paymentStatus === "PAID" || paymentStatus === "REFUNDED"
                        ? <button className="admin-toggle-btn" type="button" onClick={() => openReceipt(order)}>View receipt</button>
                        : <span className="payment-refunded-note">Refunded</span>}
                  </div>
                </article>
              );
            })}
          </div>}
    </AppShell>
  );
}
