function AuthLayout({ eyebrow, title, subtitle, children, footer }) {
    return (
        <div className="auth-shell auth-scene">
            <aside className="auth-brand">
                <div className="auth-brand-top">
                    <img className="auth-logo" src="/images/logo.jpg" alt="MediSync" />
                    <div>
                        <div className="auth-brand-name">MediSync</div>
                        <div className="auth-brand-sub">Pharmacy Prescription Management System</div>
                    </div>
                </div>

                <div className="auth-flow">
                    <div className="auth-flow-label">Every script, tracked end to end</div>
                    <ol className="auth-flow-rail">
                        {["Pending", "Received", "Preparing", "Ready for Collection", "Collected"].map((step, i) => (
                            <li key={step} className="auth-flow-step">
                                <span className="auth-flow-dot" />
                                <span className="auth-flow-text">{step}</span>
                                {i === 0 ? <span className="auth-flow-you">you are here</span> : null}
                            </li>
                        ))}
                    </ol>
                </div>

                <p className="auth-brand-foot">
                    One account, three roles — patients, doctors and admins each get their own
                    dashboard. Admins also run the pharmacy floor: inventory and prescription
                    processing.
                </p>
            </aside>

            <main className="auth-main">
                <div className="auth-card">
                    <div className="auth-card-stub">
                        <span>MEDISYNC · CPUT</span>
                        <span>ACCOUNT ACCESS</span>
                    </div>
                    <div className="auth-card-body">
                        {eyebrow ? <div className="auth-eyebrow">{eyebrow}</div> : null}
                        <h1 className="auth-title">{title}</h1>
                        {subtitle ? <p className="auth-subtitle">{subtitle}</p> : null}
                        <div className="auth-form-area">{children}</div>
                        {footer ? <div className="auth-footer">{footer}</div> : null}
                    </div>
                </div>
            </main>
        </div>
    );
}

export default AuthLayout;