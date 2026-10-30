

import { Link } from "react-router-dom";
import PublicLayout from "../components/PublicLayout";
import "../components/PublicLayout.css";

const SERVICES = [
  { icon: "📅", title: "Appointment booking", body: "Patients pick a doctor and a time that suits them, and manage or cancel their bookings any time." },
  { icon: "💊", title: "e-Prescriptions", body: "Doctors issue prescriptions directly from a consultation, with dosage, frequency and quantity per item." },
  { icon: "📍", title: "Prescription tracking", body: "A prescription's status is visible end to end — pending, received, preparing, ready, collected." },
  { icon: "🔔", title: "Notifications", body: "Patients are notified as their prescription moves through the pipeline." },
  { icon: "📊", title: "Reports & statistics", body: "Admins get a live view of patients, appointments, prescriptions and revenue." },
  { icon: "🔒", title: "Secure, role-based access", body: "Patients, doctors and admins each see exactly what's relevant to their role — nothing more." },
];

export default function Services() {
  return (
    <PublicLayout>
      <section className="pub-section pub-page-head">
        <img className="pub-page-photo" src="/images/shelf.jpg" alt="Pharmacy stock ready for dispensing" />
        <div>
          <div className="pub-eyebrow">Services</div>
          <h1 style={{ maxWidth: "24ch" }}>Everything a small pharmacy practice needs, in one system</h1>
        </div>
      </section>

      <section className="pub-section" style={{ paddingTop: 0 }}>
        <div className="pub-grid-3">
          {SERVICES.map((s) => (
            <div className="pub-card" key={s.title}>
              <div className="pub-card-icon">{s.icon}</div>
              <h3>{s.title}</h3>
              <p>{s.body}</p>
            </div>
          ))}
        </div>
      </section>

      <div className="pub-cta">
        <h2>See it for yourself</h2>
        <p>Register as a patient, doctor, or admin and explore your dashboard.</p>
        <Link to="/register" className="public-btn public-btn-primary">
          Create your account
        </Link>
      </div>
    </PublicLayout>
  );
}