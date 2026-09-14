import { createContext, useContext, useEffect, useState } from "react";
import * as authApi from "../api/authApi";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => {
        const stored = localStorage.getItem("rx_user");
        return stored ? JSON.parse(stored) : null;
    });
    const [ready, setReady] = useState(false);

    useEffect(() => {
        // On page load, if we have a token, refresh full profile details
        // (linkedProfileId etc. aren't in the login response, only /api/profile/me).
        if (localStorage.getItem("rx_token")) {
            refreshProfile().finally(() => setReady(true));
        } else {
            setReady(true);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    function persistUser(sessionUser) {
        localStorage.setItem("rx_user", JSON.stringify(sessionUser));
        setUser(sessionUser);
    }

    function applySession(authResponse) {
        const sessionUser = {
            username: authResponse.username,
            email: authResponse.email,
            roles: authResponse.roles || [],
        };
        localStorage.setItem("rx_token", authResponse.token);
        persistUser(sessionUser);
        return sessionUser;
    }

    async function refreshProfile() {
        try {
            const profile = await authApi.getMyProfile();
            const sessionUser = {
                username: profile.username,
                email: profile.email,
                firstName: profile.firstName,
                lastName: profile.lastName,
                linkedProfileId: profile.linkedProfileId,
                roles: (profile.roles || []).map((r) => r.name),
            };
            persistUser(sessionUser);
            return sessionUser;
        } catch {
            return null;
        }
    }

    async function login(credentials) {
        const res = await authApi.login(credentials);
        applySession(res);
        return refreshProfile();
    }

    async function register(details) {
        const res = await authApi.register(details);
        applySession(res);
        return refreshProfile();
    }

    function logout() {
        localStorage.removeItem("rx_token");
        localStorage.removeItem("rx_user");
        setUser(null);
    }

    function primaryRole() {
        if (!user || !user.roles || user.roles.length === 0) return null;
        // Roles come back as e.g. "ROLE_PATIENT" — strip the prefix for display/routing.
        return user.roles[0].replace(/^ROLE_/, "");
    }

    // Appointment.patientId / .doctorId aren't foreign-keyed to anything, so for this
    // capstone build we key them off the account's linkedProfileId when set, falling
    // back to the username so booking still works for accounts created without one.
    function profileId() {
        return user?.linkedProfileId || user?.username || null;
    }

    const value = { user, ready, login, register, logout, primaryRole, profileId, refreshProfile };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
    return ctx;
}
