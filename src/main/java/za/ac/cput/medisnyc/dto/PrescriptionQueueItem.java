package za.ac.cput.medisnyc.dto;

import za.ac.cput.medisnyc.domain.PrescriptionProcessingStatus;
import za.ac.cput.medisnyc.domain.PrescriptionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PrescriptionQueueItem(
        String prescriptionId,
        String patientId,
        String patientName,
        String doctorId,
        String doctorName,
        LocalDate dateIssued,
        LocalDateTime createdAt,
        PrescriptionStatus status,
        PrescriptionProcessingStatus processingStatus,
        String linkedOrderId,
        List<Medicine> medicines
) {
    public record Medicine(String medicationId, String name, int quantity) {
    }
}
