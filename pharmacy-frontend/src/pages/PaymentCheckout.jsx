import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import AppShell from "../components/AppShell";
import { Alert, Field } from "../components/Field";
import * as orderApi from "../api/orderApi";
import * as paymentApi from "../api/paymentApi";
import { parseApiError } from "../utils/parseApiError";
import { useAuth } from "../context/useAuth";
import "./Payments.css";

const TEST_CARDS = [
  { number: "4242 4242 4242 4242", result: "Success" },
  { number: "4000 0000 0000 0002", result: "Declined" },
  { number: "4000 0000 0000 9995", result: "Insufficient funds" },
  { number: "4000 0000 0000 0069", result: "Expired card" },
];

function brandFor(number) {
  const digits = number.replace(/\D/g, "");
  if (/^4/.test(digits)) return "Visa";
  if (/^(5[1-5]|2[2-7])/.test(digits)) return "Mastercard";
  if (/^3[47]/.test(digits)) return "Amex";
  return "";
}

function passesLuhn(number) {
  const digits = number.replace(/\D/g, "");
  if (!/^\d{12,19}$/.test(digits)) return false;
  let sum = 0;
  let doubleDigit = false;
  for (let index = digits.length - 1; index >= 0; index -= 1) {
    let digit = Number(digits[index]);
    if (doubleDigit && (digit *= 2) > 9) digit -= 9;
    sum += digit;
    doubleDigit = !doubleDigit;
  }
  return sum % 10 === 0;
}

function formatCardNumber(value) {
  return value.replace(/\D/g, "").slice(0, 19).replace(/(.{4})/g, "$1 ").trim();
}

function friendlyPaymentError(error) {
  const code = error?.response?.data?.code;
  const messages = {
    CARD_DECLINED: "Your card was declined. Check the details or try another card.",
    INSUFFICIENT_FUNDS: "This card has insufficient funds. Try another card.",
    EXPIRED_CARD: "This card has expired. Check the expiry date or use another card.",
    INVALID_CARD: "The card details are invalid. Please check and try again.",
    DEMO_CARD_ONLY: "Demo mode accepts only the listed test cards.",
  };
  if (code === "CARD_DECLINED" || code === "INSUFFICIENT_FUNDS" || code === "EXPIRED_CARD"
      || code === "INVALID_CARD" || code === "DEMO_CARD_ONLY") return messages[code];
  if (error?.response?.status === 429) return "Too many failed attempts. Please wait before trying again.";
  if (error?.response?.status === 409) return "This order is already paid or can no longer be paid.";
  return parseApiError(error).message;
}

function newIdempotencyKey() {
  return globalThis.crypto?.randomUUID?.() || `${Date.now()}-${Math.random().toString(36).slice(2)}`;
}

function useCheckout(orderId) {
  const [order, setOrder] = useState(null);
  const [receipt, setReceipt] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [showCards, setShowCards] = useState(false);
  const [cardholderName, setCardholderName] = useState("");
  const [cardNumber, setCardNumber] = useState("");
  const [expiry, setExpiry] = useState("");
  const [cvv, setCvv] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [idempotencyKey, setIdempotencyKey] = useState(newIdempotencyKey);
  const cardBrand = useMemo(() => brandFor(cardNumber), [cardNumber]);

  useEffect(() => {
    let active = true;
    orderApi.getOrderById(orderId)
      .then((data) => { if (active) setOrder(data); })
      .catch((err) => { if (active) setLoadError(parseApiError(err).message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [orderId]);

  async function submit(event) {
    event.preventDefault();
    if (busy) return;
    const digits = cardNumber.replace(/\D/g, "");
    const expiryDigits = expiry.replace(/\D/g, "");
    const nextErrors = {};
    if (!cardholderName.trim()) nextErrors.cardholderName = "Enter the name on the card.";
    if (!/^\d{12,19}$/.test(digits)) nextErrors.cardNumber = "Enter a card number (12 to 19 digits).";
    if (expiryDigits.length !== 4) {
      nextErrors.expiry = "Enter expiry as MM/YY.";
    } else {
      const month = Number(expiryDigits.slice(0, 2));
      const year = 2000 + Number(expiryDigits.slice(2));
      const now = new Date();
      if (month < 1 || month > 12 || year < now.getFullYear()
          || (year === now.getFullYear() && month < now.getMonth() + 1)) {
        nextErrors.expiry = "This card has expired.";
      }
    }
    if (!/^\d{3,4}$/.test(cvv)) nextErrors.cvv = "CVV must be 3 or 4 digits.";
    setFieldErrors(nextErrors);
    setError("");
    if (Object.keys(nextErrors).length) return;

    setBusy(true);
    try {
      const payment = await paymentApi.payByCard({
        orderId,
        cardholderName: cardholderName.trim(),
        cardNumber: digits,
        expiryMonth: Number(expiryDigits.slice(0, 2)),
        expiryYear: 2000 + Number(expiryDigits.slice(2)),
        cvv,
        idempotencyKey,
      });
      const paymentReceipt = await paymentApi.getReceipt(payment.paymentId);
      setReceipt(paymentReceipt);
    } catch (requestError) {
      setError(friendlyPaymentError(requestError));
      setIdempotencyKey(newIdempotencyKey());
    } finally {
      setCardholderName("");
      setCardNumber("");
      setExpiry("");
      setCvv("");
      setBusy(false);
    }
  }

  function fillCard(number) {
    setCardNumber(formatCardNumber(number));
    setCardholderName("Test Patient");
    setExpiry("12/30");
    setCvv("123");
    setFieldErrors({});
    setError("");
  }

  return {
    order, receipt, loading, loadError, busy, error, showCards, setShowCards,
    cardholderName, setCardholderName, cardNumber, setCardNumber, expiry, setExpiry,
    cvv, setCvv, fieldErrors, cardBrand, submit, fillCard,
  };
}

function ReceiptView({ receipt, onBack }) {
  return (
    <section className="payment-receipt">
      <div className="payment-success-mark" aria-hidden="true">✓</div>
      <p className="appts-eyebrow">Payment successful</p>
      <h1>Receipt</h1>
      <p className="payment-receipt-number">{receipt.receiptNumber}</p>
      <p>{receipt.date ? new Date(receipt.date).toLocaleString() : ""} · {receipt.status}</p>
      <ReceiptItems receipt={receipt} />
      <div className="payment-receipt-actions no-print">
        <button className="btn-primary" type="button" onClick={() => window.print()}>Print receipt</button>
        <button className="admin-toggle-btn" type="button" onClick={onBack}>Back to my orders</button>
      </div>
    </section>
  );
}

export function ReceiptItems({ receipt }) {
  return (
    <div className="payment-receipt-paper">
      <div className="payment-receipt-patient">{receipt.patientName}</div>
      <table className="payment-lines">
        <thead><tr><th>Medicine</th><th>Qty</th><th>Unit price</th><th>Line total</th></tr></thead>
        <tbody>{(receipt.items || []).map((item, index) => (
          <tr key={`${item.medicine}-${index}`}>
            <td>{item.medicine}</td><td>{item.quantity}</td><td>R{Number(item.unitPrice).toFixed(2)}</td>
            <td>R{Number(item.lineTotal).toFixed(2)}</td>
          </tr>
        ))}</tbody>
      </table>
      <div className="payment-total-row"><span>Subtotal (excl. VAT)</span><strong>R{Number(receipt.subtotalExVat).toFixed(2)}</strong></div>
      <div className="payment-total-row"><span>VAT 15%</span><strong>R{Number(receipt.vatAmount).toFixed(2)}</strong></div>
      <div className="payment-total-row payment-grand-total"><span>Total</span><strong>R{Number(receipt.total).toFixed(2)}</strong></div>
      <div className="payment-receipt-footer">{receipt.cardBrand} ending {receipt.cardLast4} · {receipt.status}</div>
    </div>
  );
}


function displayCardNumber(value) {
  const digits = value.replace(/\D/g, "").slice(0, 16).split("");
  const slots = Array.from({ length: 16 }, (_, index) => digits[index] || "•");
  return [0, 4, 8, 12].map((start) => slots.slice(start, start + 4).join("")).join(" ");
}

function DemoCard({ name, number, expiry, brand, flipped }) {
  const shownName = name.trim() || "YOUR NAME";
  const shownExpiry = expiry || "MM/YY";
  const shownBrand = brand || "DEMO";
  return (
    <div className={`demo-card-scene${flipped ? " is-flipped" : ""}`} aria-hidden="true">
      <div className="demo-card">
        <div className="demo-card-face demo-card-front">
          <div className="demo-card-top">
            <span className="demo-card-chip" />
            <span className="demo-card-brand">{shownBrand}</span>
          </div>
          <div className="demo-card-number">{displayCardNumber(number)}</div>
          <div className="demo-card-bottom">
            <span>
              <small>Cardholder</small>
              {shownName}
            </span>
            <span>
              <small>Expires</small>
              {shownExpiry}
            </span>
          </div>
          <span className="demo-card-watermark">DEMO</span>
        </div>
        <div className="demo-card-face demo-card-back">
          <div className="demo-card-stripe" />
          <div className="demo-card-cvv-row">
            <span>CVV</span>
            <strong>•••</strong>
          </div>
          <p>MediSync demo card · no real charge</p>
        </div>
      </div>
    </div>
  );
}

function CheckoutForm({ state, onClose }) {
  const {
    order, receipt, loading, loadError, busy, error, showCards, setShowCards,
    cardholderName, setCardholderName, cardNumber, setCardNumber, expiry, setExpiry,
    cvv, setCvv, fieldErrors, cardBrand, submit, fillCard,
  } = state;
  const [cvvFocus, setCvvFocus] = useState(false);
  if (receipt) return <ReceiptView receipt={receipt} onBack={onClose} />;
  if (loading) return <p className="appts-loading">Loading order…</p>;
  if (loadError) return <Alert>{loadError}</Alert>;
  if (!order) return <Alert>Order not found.</Alert>;

  return (
    <div className="payment-checkout-grid">
      <section className="payment-summary">
        <p className="appts-eyebrow">Order {order.orderId}</p>
        <h2>Order summary</h2>
        <p className="payment-date">{order.orderDate ? new Date(order.orderDate).toLocaleDateString() : ""}</p>
        <div className="payment-summary-items">
          {(order.items || []).map((item) => (
            <div className="payment-summary-item" key={item.orderItemId}>
              <span>{item.medicationName || item.medicine || item.medicationId} × {item.quantity}</span>
              <strong>R{Number(item.subtotal ?? item.price * item.quantity).toFixed(2)}</strong>
            </div>
          ))}
          {!order.items?.length && <p>No order items found.</p>}
        </div>
        <div className="payment-total-row payment-grand-total">
          <span>Total <small>VAT 15% included</small></span>
          <strong>R{Number(order.totalAmount || 0).toFixed(2)}</strong>
        </div>
      </section>

      <section className="payment-form-card">
        <h2>Pay by card</h2>
        <div className="payment-demo-banner">
          <strong>DEMO MODE</strong> — no real money is charged. Use a test card.
          <button type="button" className="payment-demo-toggle" onClick={() => setShowCards((value) => !value)}>
            {showCards ? "Hide test cards" : "Show test cards"}
          </button>
          {showCards && <div className="payment-test-cards">
            {TEST_CARDS.map((card) => (
              <div className="payment-test-card" key={card.number}>
                <span><strong>{card.number}</strong><small>{card.result}</small></span>
                <button type="button" onClick={() => fillCard(card.number)}>Fill in</button>
              </div>
            ))}
          </div>}
        </div>
        <DemoCard
          name={cardholderName}
          number={cardNumber}
          expiry={expiry}
          brand={cardBrand}
          flipped={cvvFocus}
        />
        <Alert>{error}</Alert>
        <form onSubmit={submit} noValidate>
          <Field id="cardholderName" label="Cardholder name" value={cardholderName}
            autoComplete="cc-name" onChange={(event) => setCardholderName(event.target.value)}
            error={fieldErrors.cardholderName} />
          <Field id="cardNumber" label={`Card number${cardBrand ? ` · ${cardBrand}` : ""}`}
            value={cardNumber} inputMode="numeric" autoComplete="cc-number"
            onChange={(event) => setCardNumber(formatCardNumber(event.target.value))}
            error={fieldErrors.cardNumber} />
          <div className="payment-card-fields">
            <Field id="expiry" label="Expiry MM/YY" value={expiry} inputMode="numeric"
              autoComplete="cc-exp" placeholder="MM/YY"
              onChange={(event) => {
                const digits = event.target.value.replace(/\D/g, "").slice(0, 4);
                setExpiry(digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits);
              }} error={fieldErrors.expiry} />
            <Field id="cvv" label="CVV" value={cvv} type="password" inputMode="numeric"
              autoComplete="cc-csc" maxLength={4}
              onFocus={() => setCvvFocus(true)}
              onBlur={() => setCvvFocus(false)}
              onChange={(event) => setCvv(event.target.value.replace(/\D/g, "").slice(0, 4))}
              error={fieldErrors.cvv} />
          </div>
          <button className="btn-primary payment-submit" type="submit" disabled={busy || !order.items?.length}>
            {busy ? <><span className="payment-spinner" /> Processing payment...</> : `Pay R${Number(order.totalAmount || 0).toFixed(2)}`}
          </button>
          {onClose && <button className="payment-cancel" type="button" onClick={onClose} disabled={busy}>Cancel</button>}
        </form>
      </section>
    </div>
  );
}

export function PaymentCheckoutModal({ orderId, onClose, onPaid }) {
  const state = useCheckout(orderId);
  const onPaidRef = useRef(onPaid);
  const paidNotified = useRef(false);
  useEffect(() => { onPaidRef.current = onPaid; }, [onPaid]);
  useEffect(() => {
    if (state.receipt && !paidNotified.current) {
      paidNotified.current = true;
      onPaidRef.current?.(state.receipt);
    }
  }, [state.receipt]);
  return (
    <div className="payment-modal-backdrop" role="presentation" onMouseDown={(event) => {
      if (event.target === event.currentTarget && !state.busy) onClose();
    }}>
      <div className="payment-modal" role="dialog" aria-modal="true" aria-label="Charge card">
        <button className="payment-modal-close no-print" type="button" onClick={onClose} aria-label="Close">×</button>
        <CheckoutForm state={state} onClose={onClose} />
      </div>
    </div>
  );
}

export default function PaymentCheckout() {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const { primaryRole } = useAuth();
  const state = useCheckout(orderId);
  const backPath = primaryRole() === "PATIENT" ? "/my-orders" : "/orders";
  return (
    <AppShell>
      <div className="payment-page-heading">
        <div><p className="appts-eyebrow">Secure checkout</p><h1>Pay for your order</h1></div>
        <Link className="admin-toggle-btn no-print" to={backPath}>
          {primaryRole() === "PATIENT" ? "Back to my orders" : "Back to orders"}
        </Link>
      </div>
      <CheckoutForm state={state} onClose={() => navigate(backPath)} />
    </AppShell>
  );
}
