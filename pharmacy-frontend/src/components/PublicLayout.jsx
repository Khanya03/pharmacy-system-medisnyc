
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "./PublicLayout.css";

const NAV_LINKS = [
  { to: "/", label: "Home" },
  { to: "/about", label: "About" },
  { to: "/services", label: "Services" },
  { to: "/contact", label: "Contact" },
];

export default function PublicLayout({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/", { replace: true });
  }

  return (
    <div className="public-shell">
      <header className="public-nav">
        <Link to="/" className="public-brand">
          <span className="public-mark">M</span>
          MediSync
        </Link>

        <nav className="public-nav-links">
          {NAV_LINKS.map((link) => (
            <Link key={link.to} to={link.to} className="public-nav-link">
              {link.label}
            </Link>
          ))}
        </nav>

        <div className="public-nav-actions">
          {user ? (
            <>
              <Link to="/dashboard" className="public-btn public-btn-primary">
                Dashboard
              </Link>
              <button className="public-btn public-btn-ghost" onClick={handleLogout}>
                Sign out
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="public-btn public-btn-ghost">
                Login
              </Link>
              <Link to="/register" className="public-btn public-btn-primary">
                Register
              </Link>
            </>
          )}
        </div>
      </header>

      <main>{children}</main>

      <footer className="public-footer">
        <div className="public-footer-inner">
          <div>
            <div className="public-brand">
              <span className="public-mark">M</span>
              MediSync
            </div>
            <p className="public-footer-tag">Pharmacy prescription management, from booking to collection.</p>
          </div>
          <div className="public-footer-links">
            {NAV_LINKS.map((link) => (
              <Link key={link.to} to={link.to}>
                {link.label}
              </Link>
            ))}
          </div>
        </div>
        <p className="public-footer-copy">© {new Date().getFullYear()} MediSync · CPUT PRP3 project</p>
      </footer>
    </div>
  );
}