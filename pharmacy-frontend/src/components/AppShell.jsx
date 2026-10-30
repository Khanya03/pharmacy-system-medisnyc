
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import NotificationBell from "./NotificationBell";
import "./AppShell.css";

function sceneFor(pathname) {
    if (pathname.startsWith("/consult") || pathname.startsWith("/prescription")) return "consult";
    if (pathname.startsWith("/appointment") || pathname.startsWith("/admin/patients") || pathname.startsWith("/admin/doctors")) return "counter";
    return "shelf";
}

export default function AppShell({ children }) {
    const { user, logout, primaryRole } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const role = primaryRole();
    const scene = sceneFor(location.pathname);

    function handleLogout() {
        logout();
        navigate("/login", { replace: true, state: null });
    }

    return (
        <div className={`shell shell-scene shell-scene-${scene}`}>
            <header className="shell-nav">
                <Link to="/dashboard" className="shell-brand">
                    <span className="shell-mark">M</span>
                    MediSync
                </Link>

                <div className="shell-nav-right">
                    {user && (
                        <>
                            {role === "PATIENT" && <NotificationBell />}
                            {role === "PATIENT" && (
                                <Link to="/my-orders" className="shell-link">My orders &amp; payments</Link>
                            )}
                            {(role === "ADMIN" || role === "PHARMACIST") && (
                                <>
                                    <Link to="/orders" className="shell-link">Orders</Link>
                                    <Link to="/reports" className="shell-link">Reports</Link>
                                </>
                            )}
                            <span className="shell-role-badge">{role}</span>
                            <Link to="/profile" className="shell-link">
                                {user.username}
                            </Link>
                            <button className="shell-logout" onClick={handleLogout}>
                                Sign out
                            </button>
                        </>
                    )}
                </div>
            </header>

            <main className="shell-content">{children}</main>
        </div>
    );
}