
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import NotificationBell from "./NotificationBell";
import "./AppShell.css";

export default function AppShell({ children }) {
    const { user, logout, primaryRole } = useAuth();
    const navigate = useNavigate();
    const role = primaryRole();

    function handleLogout() {
        logout();
        navigate("/login", { replace: true });
    }

    return (
        <div className="shell">
            <header className="shell-nav">
                <Link to="/dashboard" className="shell-brand">
                    <span className="shell-mark">M</span>
                    MediSync
                </Link>

                <div className="shell-nav-right">
                    {user && (
                        <>
                            {role === "PATIENT" && <NotificationBell />}
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