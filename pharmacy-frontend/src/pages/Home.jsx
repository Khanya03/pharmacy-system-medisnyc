import { Link } from "react-router-dom";
import PublicLayout from "../components/PublicLayout";
import "../components/PublicLayout.css";

const FEATURES = [
  { icon: "📅", title: "Appointment booking", body: "Patients pick a doctor and a time slot; doctors see their day at a glance." },
  { icon: "💊", title: "e-Prescriptions", body: "Doctors issue prescriptions straight from a consultation, medication by medication." },
  { icon: "📦", title: "Pharmacy inventory", body: "Stock levels, batches and expiry dates, with automatic low-stock alerts." },
  { icon: "🔔", title: "Real-time tracking", body: "Every prescription's status is visible to the patient, from pending to collected." },
];

const ROLES = [
  { title: "Patients", body: "Book appointments, track prescriptions, and pay for ready orders." },
  { title: "Doctors", body: "Run consultations, record diagnoses, and issue prescriptions in one flow." },
  { title: "Admins", body: "Run the pharmacy floor — inventory, processing, users and reports." },
];

const STEPS = [
  { title: "Register", body: "Create an account as a patient, doctor, or admin." },
  { title: "Book or consult", body: "Patients book with a doctor; doctors run the consultation." },
  { title: "Prescribe", body: "The doctor issues a prescription with the medications needed." },
  { title: "Track & collect", body: "The pharmacy fills it, and the patient watches it move to ready for collection." },
];

export default function Home() {
  return (
    <PublicLayout>
      <section className="pub-hero pub-hero-split">
        <div className="pub-hero-copy">
          <div className="pub-eyebrow">MediSync</div>
          <h1>Pharmacy care, from booking to collection</h1>
          <p>
            Patients, doctors and pharmacy staff work from the same prescription —
            no phone calls, no guessing whether it is ready.
          </p>
          <div className="pub-hero-actions">
            <Link to="/register" className="public-btn public-btn-primary">Get started</Link>
            <Link to="/login" className="public-btn public-btn-ghost">Login</Link>
          </div>
        </div>
        <figure className="pub-hero-figure">
          <img src="/images/counter.jpg" alt="Pharmacist handing a prepared order to a patient" />
        </figure>
      </section>

      <section className="pub-section">
        <div className="pub-eyebrow">What it does</div>
        <div className="pub-grid-4">
          {FEATURES.map((f) => (
            <div className="pub-card" key={f.title}>
              <div className="pub-card-icon">{f.icon}</div>
              <h3>{f.title}</h3>
              <p>{f.body}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="pub-band">
        <img src="/images/shelf.jpg" alt="Organised pharmacy shelves" />
        <div>
          <div className="pub-eyebrow">Inventory</div>
          <h2>Stock you can trust</h2>
          <p>Batches, expiry dates and low-stock alerts stay visible, so the counter is never guessing.</p>
        </div>
      </section>

      <section className="pub-section">
        <div className="pub-eyebrow">Built for three roles</div>
        <div className="pub-grid-3">
          {ROLES.map((r) => (
            <div className="pub-card" key={r.title}>
              <h3>{r.title}</h3>
              <p>{r.body}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="pub-section pub-consult" style={{ paddingTop: 0 }}>
        <img src="/images/consult.jpg" alt="Consultation notes, stethoscope and dispensed medicine" />
        <div>
          <div className="pub-eyebrow">How it works</div>
          <div className="pub-steps">
            {STEPS.map((s, i) => (
              <div className="pub-step" key={s.title}>
                <div className="pub-step-num">{String(i + 1).padStart(2, "0")}</div>
                <h3>{s.title}</h3>
                <p>{s.body}</p>
              </div>
            ))}
          </div>
        </div>
      </section>
    </PublicLayout>
  );
}
