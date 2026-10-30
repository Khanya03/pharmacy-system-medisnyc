import PublicLayout from "../components/PublicLayout";
import "../components/PublicLayout.css";

const VALUES = [
    { title: "Clarity", body: "Every status is visible — nobody should have to call to ask where a prescription is." },
    { title: "Safety", body: "Role-based access keeps medical and prescribing actions with the people qualified to take them." },
    { title: "Speed", body: "Booking, prescribing and dispensing shouldn't take more clicks than they need to." },
    { title: "Accountability", body: "An audit trail means every action taken in the system can be traced." },
];

export default function About() {
    return (
        <PublicLayout>
            <section className="pub-section pub-page-head">
                <img className="pub-page-photo" src="/images/consult.jpg" alt="A consultation ready for prescribing" />
                <div>
                <div className="pub-eyebrow">About us</div>
                <h1 style={{ maxWidth: "26ch" }}>Built to close the gap between a prescription and a patient</h1>
                <p style={{ color: "var(--ink-soft)", maxWidth: "62ch", marginTop: "16px", fontSize: "15px" }}>
                    MediSync started as a CPUT ICT Application Development capstone project: a pharmacy
                    prescription system that keeps patients, doctors and pharmacy staff working from the
                    same information, instead of three separate systems and a lot of phone calls.
                </p>
                </div>
            </section>

            <section className="pub-section" style={{ paddingTop: 0 }}>
                <div className="pub-grid-3">
                    <div className="pub-card">
                        <h3>Our story</h3>
                        <p>
                            Booking an appointment, getting a diagnosis, and collecting a prescription usually
                            involves three disconnected steps. MediSync brings them into one flow so nothing
                            gets lost between the doctor's desk and the pharmacy counter.
                        </p>
                    </div>
                    <div className="pub-card">
                        <h3>Our mission</h3>
                        <p>
                            Make it obvious, at every step, what stage a prescription is at — for the patient
                            waiting on it and the pharmacist filling it.
                        </p>
                    </div>
                    <div className="pub-card">
                        <h3>Our vision</h3>
                        <p>
                            A pharmacy workflow where booking, prescribing, dispensing and reporting all speak
                            the same language, so nobody has to re-enter the same information twice.
                        </p>
                    </div>
                </div>
            </section>

            <section className="pub-section" style={{ paddingTop: 0 }}>
                <div className="pub-eyebrow">What we care about</div>
                <div className="pub-values-grid">
                    {VALUES.map((v) => (
                        <div className="pub-value-card" key={v.title}>
                            <h4>{v.title}</h4>
                            <p>{v.body}</p>
                        </div>
                    ))}
                </div>
            </section>
        </PublicLayout>
    );
}
