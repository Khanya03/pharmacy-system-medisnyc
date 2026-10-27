import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as notificationApi from "../api/notificationApi";
import "./NotificationBell.css";

const POLL_INTERVAL_MS = 15000;

function timeAgo(isoString) {
    if (!isoString) return "";
    const diffMs = Date.now() - new Date(isoString).getTime();
    const mins = Math.floor(diffMs / 60000);
    if (mins < 1) return "just now";
    if (mins < 60) return `${mins}m ago`;
    const hours = Math.floor(mins / 60);
    if (hours < 24) return `${hours}h ago`;
    return `${Math.floor(hours / 24)}d ago`;
}

export default function NotificationBell() {
    const [notifications, setNotifications] = useState([]);
    const [open, setOpen] = useState(false);
    const [loadError, setLoadError] = useState("");
    const wrapRef = useRef(null);
    const navigate = useNavigate();

    useEffect(() => {
        function reportError(error, action) {
            console.warn(`Unable to ${action} prescription notifications`, error);
            setLoadError(error?.response?.data?.message || "Notifications could not be loaded.");
        }

        function load() {
            notificationApi.getMine()
                .then((items) => {
                    setNotifications(items);
                    setLoadError("");
                })
                .catch((error) => reportError(error, "load"));
        }

        function handleFocus() {
            load();
        }

        load();
        const timer = setInterval(load, POLL_INTERVAL_MS);
        window.addEventListener("focus", handleFocus);
        return () => {
            clearInterval(timer);
            window.removeEventListener("focus", handleFocus);
        };
    }, []);

    useEffect(() => {
        function handleClickOutside(e) {
            if (wrapRef.current && !wrapRef.current.contains(e.target)) setOpen(false);
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const unreadCount = notifications.filter((n) => !n.read).length;

    async function handleItemClick(n) {
        if (!n.read) {
            try {
                await notificationApi.markAsRead(n.notificationId);
                setNotifications((prev) =>
                    prev.map((item) => (item.notificationId === n.notificationId ? { ...item, read: true } : item))
                );
            } catch (error) {
                console.warn("Unable to mark prescription notification as read", error);
                setLoadError(error?.response?.data?.message || "This notification could not be marked as read.");
                return;
            }
        }
        setOpen(false);
        navigate("/prescriptions/track");
    }

    return (
        <div className="bell-wrap" ref={wrapRef}>
            <button
                className="bell-btn"
                onClick={() => setOpen((o) => !o)}
                aria-label="Notifications"
                type="button"
            >
                🔔
                {unreadCount > 0 && <span className="bell-count">{unreadCount > 9 ? "9+" : unreadCount}</span>}
            </button>

            {open && (
                <div className="bell-panel">
                    <div className="bell-panel-head">Notifications</div>
                    {loadError ? (
                        <div className="bell-error" role="alert">{loadError}</div>
                    ) : notifications.length === 0 ? (
                        <div className="bell-empty">Nothing yet — we'll let you know as your prescriptions move along.</div>
                    ) : (
                        notifications.map((n) => (
                            <div
                                key={n.notificationId}
                                className={`bell-item ${!n.read ? "unread" : ""}`}
                                onClick={() => handleItemClick(n)}
                            >
                                <div className="bell-item-msg">{n.message}</div>
                                <div className="bell-item-time">{timeAgo(n.createdAt)}</div>
                            </div>
                        ))
                    )}
                </div>
            )}
        </div>
    );
}
