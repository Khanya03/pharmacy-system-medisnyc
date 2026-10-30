import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import AppShell from "../components/AppShell";
import { Field, SelectField, Alert } from "../components/Field";
import * as medicationApi from "../api/medicationApi";
import * as inventoryApi from "../api/inventoryApi";
import { parseApiError } from "../utils/parseApiError";
import "../components/Form.css";
import "./Inventory.css";

const EMPTY_MED = { medicationName: "", dosageForm: "", manufacturer: "", strength: "", category: "", description: "" };
const EMPTY_STOCK = { stockLevel: "", batchNumber: "", supplier: "", unitPrice: "", expiryDate: "", receivedDate: "", reorderLevel: "10" };
const DOSAGE_FORMS = ["Tablet", "Capsule", "Syrup", "Injection", "Cream", "Drops", "Inhaler", "Ointment", "Solution", "Suspension", "Powder", "Patch"];
const CATEGORIES = ["Analgesic", "Antibiotic", "Antihistamine", "Antacid", "Antidiabetic", "Antihypertensive", "Antiviral", "Vitamin", "Vaccine"];
const STRENGTH_UNITS = ["mg", "g", "ml", "mcg", "IU", "OTHER"];

export default function AddMedicine() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const editId = searchParams.get("edit");
    const receiveMedicationId = searchParams.get("medicationId");
    const receiveMode = Boolean(receiveMedicationId);
    const [med, setMed] = useState(EMPTY_MED);
    const [strengthAmount, setStrengthAmount] = useState("");
    const [strengthUnit, setStrengthUnit] = useState("mg");
    const [stock, setStock] = useState(EMPTY_STOCK);
    const [error, setError] = useState("");
    const [fieldErrors, setFieldErrors] = useState({});
    const [submitting, setSubmitting] = useState(false);
    const [loadingMedication, setLoadingMedication] = useState(Boolean(editId || receiveMedicationId));
    const [loadedMedication, setLoadedMedication] = useState(!editId && !receiveMedicationId);

    useEffect(() => {
        const medicationId = editId || receiveMedicationId;
        if (!medicationId) return;
        medicationApi.getMedication(medicationId)
            .then((loaded) => {
                const strength = loaded.strength || "";
                const match = strength.trim().match(/^(.+?)\s*(mg|g|ml|mcg|iu)$/i);
                setStrengthAmount(match ? match[1].trim() : strength);
                setStrengthUnit(match ? STRENGTH_UNITS.find((unit) => unit.toLowerCase() === match[2].toLowerCase()) : "OTHER");
                setMed({
                    medicationName: loaded.medicationName || "",
                    dosageForm: loaded.dosageForm || "",
                    manufacturer: loaded.manufacturer || "",
                    strength,
                    category: loaded.category || "",
                    description: loaded.description || "",
                });
            })
            .then(() => setLoadedMedication(true))
            .catch((err) => setError(parseApiError(err).message))
            .finally(() => setLoadingMedication(false));
    }, [editId, receiveMedicationId]);

    function updateMed(field, value) {
        setMed((prev) => ({ ...prev, [field]: value }));
        setFieldErrors((prev) => ({ ...prev, [field]: undefined }));
    }

    function updateStock(field, value) {
        setStock((prev) => ({ ...prev, [field]: value }));
        setFieldErrors((prev) => ({ ...prev, [field]: undefined }));
    }

    function updateStrengthAmount(value) {
        setStrengthAmount(value);
        setMed((prev) => ({ ...prev, strength: strengthUnit === "OTHER" ? value : `${value} ${strengthUnit}`.trim() }));
        setFieldErrors((prev) => ({ ...prev, strength: undefined }));
    }

    function updateStrengthUnit(value) {
        setStrengthUnit(value);
        setMed((prev) => ({ ...prev, strength: value === "OTHER" ? strengthAmount : `${strengthAmount} ${value}`.trim() }));
        setFieldErrors((prev) => ({ ...prev, strength: undefined }));
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setError("");
        setFieldErrors({});
        setSubmitting(true);
        try {
            if (receiveMode) {
                await inventoryApi.receiveStock({ ...stock, medicationId: receiveMedicationId });
            } else {
                const saved = editId
                    ? await medicationApi.updateMedication(editId, med)
                    : await medicationApi.addMedication(med);
                if (!editId && stock.stockLevel !== "") {
                    await inventoryApi.receiveStock({ ...stock, medicationId: saved.medicationId });
                }
            }
            navigate("/inventory", { replace: true });
        } catch (err) {
            const parsed = parseApiError(err);
            setError(parsed.message);
            setFieldErrors(parsed.fieldErrors || {});
        } finally {
            setSubmitting(false);
        }
    }

    const title = receiveMode ? "Receive stock" : editId ? "Edit medicine" : "Add medicine";

    return (
        <AppShell>
            <h1>{title}</h1>
            <p className="appts-sub" style={{ marginBottom: "24px" }}>
                {receiveMode
                    ? `Add a new stock batch for ${med.medicationName || "this medicine"}.`
                    : "Maintain the medicine catalogue and optionally receive an initial batch."}
            </p>

            <div className="consult-card">
                <form onSubmit={handleSubmit} noValidate>
                    <Alert type="error">{error}</Alert>

                    {!receiveMode && (
                        <>
                            <Field
                                id="medicationName"
                                label="Medicine name"
                                value={med.medicationName}
                                onChange={(e) => updateMed("medicationName", e.target.value)}
                                error={fieldErrors.medicationName}
                                required
                                maxLength={100}
                            />
                            <div className="field-row">
                                <Field
                                    id="strength"
                                    label="Strength amount"
                                    type={strengthUnit === "OTHER" ? "text" : "number"}
                                    min={strengthUnit === "OTHER" ? undefined : "0.001"}
                                    step={strengthUnit === "OTHER" ? undefined : "any"}
                                    value={strengthAmount}
                                    onChange={(e) => updateStrengthAmount(e.target.value)}
                                    error={fieldErrors.strength}
                                    placeholder={strengthUnit === "OTHER" ? "Enter strength" : "e.g. 500"}
                                    required
                                />
                                <SelectField
                                    id="strengthUnit"
                                    label="Strength unit"
                                    value={strengthUnit}
                                    onChange={(e) => updateStrengthUnit(e.target.value)}
                                >
                                    {STRENGTH_UNITS.map((unit) => <option key={unit} value={unit}>{unit === "OTHER" ? "Other" : unit}</option>)}
                                </SelectField>
                                <SelectField
                                    id="dosageForm"
                                    label="Dosage form"
                                    value={med.dosageForm}
                                    onChange={(e) => updateMed("dosageForm", e.target.value)}
                                    error={fieldErrors.dosageForm}
                                    required
                                >
                                    <option value="">Select a form…</option>
                                    {med.dosageForm && !DOSAGE_FORMS.includes(med.dosageForm) && <option value={med.dosageForm}>{med.dosageForm}</option>}
                                    {DOSAGE_FORMS.map((form) => <option key={form} value={form}>{form}</option>)}
                                </SelectField>
                            </div>
                            <div className="field-row">
                                <Field
                                    id="manufacturer"
                                    label="Manufacturer"
                                    value={med.manufacturer}
                                    onChange={(e) => updateMed("manufacturer", e.target.value)}
                                />
                                <Field
                                    id="category"
                                    label="Category"
                                    value={med.category}
                                    onChange={(e) => updateMed("category", e.target.value)}
                                    error={fieldErrors.category}
                                    placeholder="Select or type a category"
                                    list="category-options"
                                    required
                                />
                                <datalist id="category-options">
                                    {CATEGORIES.map((value) => <option key={value} value={value} />)}
                                </datalist>
                            </div>
                            <Field
                                id="description"
                                label="Description (optional)"
                                value={med.description}
                                onChange={(e) => updateMed("description", e.target.value)}
                            />
                        </>
                    )}

                    {!editId && !receiveMode && (
                        <label className="inv-checkbox">
                            <input type="checkbox" checked={stock.stockLevel !== ""} onChange={(e) => updateStock("stockLevel", e.target.checked ? "0" : "")} />
                            Receive an initial batch of stock now
                        </label>
                    )}

                    {(receiveMode || (!editId && stock.stockLevel !== "")) && (
                        <>
                            <div className="field-row">
                                <Field
                                    id="stockLevel"
                                    label="Quantity"
                                    type="number"
                                    min="0"
                                    value={stock.stockLevel}
                                    onChange={(e) => updateStock("stockLevel", e.target.value)}
                                    error={fieldErrors.stockLevel}
                                    required
                                />
                                <Field
                                    id="reorderLevel"
                                    label="Reorder level"
                                    type="number"
                                    min="0"
                                    value={stock.reorderLevel}
                                    onChange={(e) => updateStock("reorderLevel", e.target.value)}
                                    error={fieldErrors.reorderLevel}
                                    required
                                />
                            </div>
                            <div className="field-row">
                                <Field
                                    id="batchNumber"
                                    label="Batch number"
                                    value={stock.batchNumber}
                                    onChange={(e) => updateStock("batchNumber", e.target.value)}
                                    error={fieldErrors.batchNumber}
                                    required
                                />
                                <Field
                                    id="supplier"
                                    label="Supplier"
                                    value={stock.supplier}
                                    onChange={(e) => updateStock("supplier", e.target.value)}
                                />
                            </div>
                            <div className="field-row">
                                <Field
                                    id="unitPrice"
                                    label="Unit price (R)"
                                    type="number"
                                    min="0"
                                    step="0.01"
                                    value={stock.unitPrice}
                                    onChange={(e) => updateStock("unitPrice", e.target.value)}
                                    error={fieldErrors.unitPrice}
                                    required
                                />
                                <Field
                                    id="expiryDate"
                                    label="Expiry date"
                                    type="date"
                                    min={new Date().toISOString().slice(0, 10)}
                                    value={stock.expiryDate}
                                    onChange={(e) => updateStock("expiryDate", e.target.value)}
                                    error={fieldErrors.expiryDate}
                                    required
                                />
                            </div>
                        </>
                    )}

                    <button className="btn-primary" type="submit" disabled={submitting || loadingMedication || !loadedMedication}>
                        {submitting ? "Saving…" : title}
                    </button>
                </form>
            </div>
        </AppShell>
    );
}
