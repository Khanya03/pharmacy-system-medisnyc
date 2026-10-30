import { Link } from "react-router-dom";
import AppShell from "../components/AppShell";
import { useAuth } from "../context/useAuth";
import "./Dashboard.css";

const ROLE_COPY = {
    PATIENT: "Book and track appointments, and follow your prescriptions from pending to collected.",
    DOCTOR: "Run consultations and issue prescriptions for your patients.",
    ADMIN: "Run the pharmacy floor and oversee the system — inventory, prescriptions, users and reports.",
    PHARMACIST: "Run the pharmacy floor and oversee the system — inventory, prescriptions, users and reports.",
};

function tilesForRole(role) {
    const tiles = [
        { to: "/profile", title: "Your profile", body: "Update your name and email, and see the roles on your account." },
    ];

    if (role === "PATIENT") {
        tiles.push(
            { to: "/appointments", title: "Your appointments", body: "Book a new appointment, or view and cancel upcoming ones." },
            { to: "/prescriptions/track", title: "Track prescriptions", body: "Follow your prescriptions from pending to ready for collection." },
            { to: "/my-orders", title: "My orders & payments", body: "Pay for an order, review payment status, and view receipts." },
        );
    }

    if (role === "DOCTOR") {
        tiles.push(
            { to: "/consultations", title: "Today's appointments", body: "Start a consultation and issue a prescription." },
            { to: "/consultations/patients", title: "Your patients", body: "Everyone you've seen, at a glance." },
            { to: "/prescriptions/mine", title: "Prescriptions you've issued", body: "Track each one from pending to collected." },
            { to: "/appointments", title: "All your appointments", body: "See everything booked with you, past and upcoming." }
        );
    }

    // Admin now covers pharmacist duties too — one role runs both the pharmacy
    // floor and system administration. PHARMACIST is kept only so any existing
    // account with that legacy role still gets full access.
    if (role === "ADMIN" || role === "PHARMACIST") {
        tiles.push(
            { to: "/admin/doctors", title: "Doctors", body: "Add a doctor so patients can book with them." },
            { to: "/admin/patients", title: "Patients", body: "See everyone registered, or add a walk-in patient." },
            { to: "/appointments", title: "Appointments", body: "See every appointment in the system." },
            { to: "/inventory", title: "Inventory", body: "Manage the medicine catalogue and stock levels." },
            { to: "/prescriptions/queue", title: "Prescription queue", body: "Move prescriptions through Pending → Collected." },
            { to: "/prescriptions/ready", title: "Ready for collection", body: "Hand off prescriptions waiting at the counter." },
            { to: "/orders", title: "Orders & payments", body: "Charge orders, print receipts, and process refunds." },
            { to: "/reports", title: "Reports", body: "Review revenue, payment activity, and export reports." },
            { to: "/admin", title: "Admin dashboard", body: "System statistics, user management, reports and audit log." }
        );
    }

    return tiles;
}

export default function Dashboard() {
    const { user, primaryRole } = useAuth();
    const role = primaryRole();
    const tiles = tilesForRole(role);

    return (
        <AppShell>
            <div className="dash-page">
            <div className="dash-eyebrow">Dashboard</div>
            <h1>Welcome back, {user?.username}</h1>
            <p className="dash-sub">{ROLE_COPY[role] || "Your role determines what you can do next."}</p>

            <div className="dash-grid">
                {tiles.map((tile) => (
                    <Link to={tile.to} className="dash-tile" key={tile.to + tile.title}>
                        <div className="dash-tile-title">{tile.title}</div>
                        <p>{tile.body}</p>
                    </Link>
                ))}
            </div>
            </div>
        </AppShell>
    );
}