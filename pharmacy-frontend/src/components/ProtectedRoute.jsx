import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function ProtectedRoute({ children, roles }) {
    const { user, ready } = useAuth();
    const location = useLocation();

    if (!ready) return null;

    if (!user) {
        return <Navigate to="/login" replace state={{ from: location.pathname }} />;
    }

    if (roles && roles.length > 0) {
        const userRoles = (user.roles || []).map((r) => r.replace(/^ROLE_/, ""));
        const allowed = roles.some((r) => userRoles.includes(r));
        if (!allowed) {
            return <Navigate to="/dashboard" replace />;
        }
    }

    return children;
}