import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import AppShell from "../components/AppShell";
import { Alert } from "../components/Field";
import * as paymentApi from "../api/paymentApi";
import { parseApiError } from "../utils/parseApiError";
import { useAuth } from "../context/useAuth";
import { ReceiptItems } from "./PaymentCheckout";
import "./Payments.css";

export default function PaymentReceipt() {
  const { paymentId } = useParams();
  const { primaryRole } = useAuth();
  const isPatient = primaryRole() === "PATIENT";
  const [receipt, setReceipt] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  useEffect(() => {
    let active = true;
    paymentApi.getReceipt(paymentId).then((data) => { if (active) setReceipt(data); })
      .catch((requestError) => { if (active) setError(parseApiError(requestError).message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [paymentId]);

  return (
    <AppShell>
      <div className="payment-receipt-page">
        <Alert>{error}</Alert>
        {loading ? <p className="appts-loading">Loading receipt…</p> : receipt && (
          <section className="payment-receipt">
            <p className="appts-eyebrow">Payment receipt</p>
            <h1>{receipt.receiptNumber}</h1>
            <p>{receipt.date ? new Date(receipt.date).toLocaleString() : ""}</p>
            <ReceiptItems receipt={receipt} />
            <div className="payment-receipt-actions no-print">
              <button className="btn-primary" type="button" onClick={() => window.print()}>Print receipt</button>
              <Link className="admin-toggle-btn" to={isPatient ? "/my-orders" : "/orders"}>
                {isPatient ? "Back to my orders" : "Back to orders"}
              </Link>
            </div>
          </section>
        )}
      </div>
    </AppShell>
  );
}
