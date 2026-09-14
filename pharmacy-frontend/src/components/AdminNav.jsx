import { NavLink } from "react-router-dom";
import "../pages/Admin.css";

const LINKS = [
    { to: "/admin", label: "Overview", end: true },
    { to: "/admin/users", label: "User management" },
    { to: "/admin/doctors", label: "Doctors" },
    { to: "/admin/patients", label: "Patients" },
    { to: "/admin/orders", label: "Orders" },
    { to: "/admin/reports", label: "Reports" },
    { to: "/admin/audit-log", label: "Audit log" },
];

export default function AdminNav() {
    return (
        <nav className="admin-nav">
            {LINKS.map((link) => (
                <NavLink
                    key={link.to}
                    to={link.to}
                    end={link.end}
                    className={({ isActive }) => `admin-nav-link ${isActive ? "active" : ""}`}
                >
                    {link.label}
                </NavLink>
            ))}
        </nav>
    );
}
