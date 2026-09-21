
import "./StatusBadge.css";

const STATUS_STYLES = {
  // Appointment statuses
  SCHEDULED: "status-amber",
  CONFIRMED: "status-pine",
  COMPLETED: "status-sage",
  CANCELLED: "status-error",
  // Prescription processing statuses (Module 5 flow)
  PENDING: "status-amber",
  RECEIVED: "status-sage",
  PREPARING: "status-amber",
  READY_FOR_COLLECTION: "status-pine",
  COLLECTED: "status-sage",
  // Prescription clinical status
  ACTIVE: "status-pine",
  EXPIRED: "status-error",
};

function label(status) {
  return status ? status.replace(/_/g, " ") : "";
}

export default function StatusBadge({ status }) {
  const cls = STATUS_STYLES[status] || "status-sage";
  return <span className={`status-badge ${cls}`}>{label(status)}</span>;
}