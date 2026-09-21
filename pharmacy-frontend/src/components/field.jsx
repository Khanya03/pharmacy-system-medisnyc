field.jsx
export function Field({ label, error, ...inputProps }) {
  return (
    <div className="field">
      <label className="field-label" htmlFor={inputProps.id}>
        {label}
      </label>
      <input className={`field-input ${error ? "has-error" : ""}`} {...inputProps} />
      {error && <span className="field-error">{error}</span>}
    </div>
  );
}

export function SelectField({ label, error, children, ...selectProps }) {
  return (
    <div className="field">
      <label className="field-label" htmlFor={selectProps.id}>
        {label}
      </label>
      <select className={`field-select ${error ? "has-error" : ""}`} {...selectProps}>
        {children}
      </select>
      {error && <span className="field-error">{error}</span>}
    </div>
  );
}

export function Alert({ type = "error", children }) {
  if (!children) return null;
  return <div className={`alert alert-${type}`}>{children}</div>;
}