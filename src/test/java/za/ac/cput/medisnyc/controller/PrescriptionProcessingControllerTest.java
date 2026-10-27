package za.ac.cput.medisnyc.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import za.ac.cput.medisnyc.exception.GlobalExceptionHandler;
import za.ac.cput.medisnyc.exception.InsufficientStockException;
import za.ac.cput.medisnyc.exception.ResourceNotFoundException;
import za.ac.cput.medisnyc.service.PrescriptionProcessingService;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PrescriptionProcessingControllerTest {

    private PrescriptionProcessingService processingService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        processingService = mock(PrescriptionProcessingService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PrescriptionProcessingController(processingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void insufficientStockReturnsConflictWithMedicineNames() throws Exception {
        when(processingService.advanceStatus("RX-1"))
                .thenThrow(new InsufficientStockException(List.of("Gaviscon", "Folic Acid")));

        mockMvc.perform(put("/api/prescription-processing/RX-1/advance"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Insufficient stock for medications: Gaviscon, Folic Acid"))
                .andExpect(jsonPath("$.outOfStockMedicines[0]").value("Gaviscon"))
                .andExpect(jsonPath("$.outOfStockMedicines[1]").value("Folic Acid"));
    }

    @Test
    void missingPrescriptionReturnsNotFoundWithMessage() throws Exception {
        when(processingService.getById("RX-missing"))
                .thenThrow(new ResourceNotFoundException("Prescription not found: RX-missing"));

        mockMvc.perform(get("/api/prescription-processing/RX-missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Prescription not found: RX-missing"));
    }
}
