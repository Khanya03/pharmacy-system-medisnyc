package za.ac.cput.medisnyc.exception;

import java.util.List;

public class InsufficientStockException extends RuntimeException {

    private final List<String> medicineNames;

    public InsufficientStockException(List<String> medicineNames) {
        super("Insufficient stock for medications: " + String.join(", ", medicineNames));
        this.medicineNames = List.copyOf(medicineNames);
    }

    public List<String> getMedicineNames() {
        return medicineNames;
    }
}
