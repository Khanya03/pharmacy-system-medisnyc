package za.ac.cput.medisnyc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import za.ac.cput.medisnyc.dto.CreateMedicationRequest;
import za.ac.cput.medisnyc.dto.ReceiveStockRequest;
import za.ac.cput.medisnyc.repository.jpa.InventoryJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.MedicationJpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@Order(2)
public class MedicationDataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MedicationDataSeeder.class);
    private static final String[] SUPPLIERS = {
            "Alpha Pharm", "Transpharm", "UPD", "Medipost Wholesale"
    };
    private static final List<MedicationSeed> SAMPLE_MEDICATIONS = List.of(
            // Analgesic
            med("Analgesic", "Adco-Ibuprofen", "200", "mg", "Tablet", "Adcock Ingram", "Ibuprofen for relief of mild pain and inflammation.", 48, 0, 260, "12.50"),
            med("Analgesic", "Aspirin Bayer", "300", "mg", "Tablet", "Bayer", "Low-dose-free aspirin tablets for pain and fever.", 72, 0, 20, "18.99"),
            med("Analgesic", "Nurofen", "200", "mg", "Capsule", "Reckitt", "Ibuprofen soft capsules for short-term pain relief.", 60, 0, 280, "42.50"),
            med("Analgesic", "Cataflam", "25", "mg", "Tablet", "Novartis", "Diclofenac potassium tablets for pain and inflammation.", 24, 24, 320, "56.00"),
            med("Analgesic", "Paracetamol Generic", "500", "mg", "Tablet", "Aspen Pharmacare", "Paracetamol tablets for relief of pain and fever.", 6, 0, 240, "9.99"),

            // Antibiotic
            med("Antibiotic", "Amoxil", "500", "mg", "Capsule", "GSK", "Amoxicillin capsules for clinician-prescribed bacterial infections.", 30, 30, 300, "89.00"),
            med("Antibiotic", "Augmentin", "625", "mg", "Tablet", "GSK", "Amoxicillin and clavulanic acid tablets.", 40, 0, 280, "178.00"),
            med("Antibiotic", "Zinnat", "250", "mg", "Tablet", "GSK", "Cefuroxime axetil tablets for prescribed treatment.", 30, 0, 290, "112.00"),
            med("Antibiotic", "Ciprobay", "500", "mg", "Tablet", "Bayer", "Ciprofloxacin tablets for clinician-directed treatment.", 20, 20, 310, "99.00"),
            med("Antibiotic", "Keflex", "500", "mg", "Capsule", "Aspen Pharmacare", "Cephalexin capsules for prescribed bacterial infections.", 36, 0, 275, "74.50"),

            // Antihistamine
            med("Antihistamine", "Cetirizine Cipla", "10", "mg", "Tablet", "Cipla", "Non-drowsy cetirizine tablets for allergy symptoms.", 90, 0, 300, "15.00"),
            med("Antihistamine", "Clarityne", "10", "mg", "Tablet", "Bayer", "Loratadine tablets for seasonal allergy symptoms.", 60, 0, 285, "49.99"),
            med("Antihistamine", "Telfast", "120", "mg", "Tablet", "Sanofi", "Fexofenadine tablets for allergic rhinitis.", 48, 0, 310, "78.00"),
            med("Antihistamine", "Zyrtec", "10", "mg", "Tablet", "UCB", "Cetirizine tablets for hay fever and hives.", 36, 36, 330, "69.50"),
            med("Antihistamine", "Allergex Non-Drowsy", "5", "mg", "Tablet", "Adcock Ingram", "Levocetirizine tablets for allergy relief.", 32, 0, 290, "42.00"),

            // Antacid
            med("Antacid", "Gaviscon", "10", "ml", "Suspension", "Reckitt", "Alginate antacid suspension for heartburn and reflux.", 30, 0, 270, "58.00"),
            med("Antacid", "Rennie", "680", "mg", "Tablet", "Bayer", "Calcium and magnesium chewable antacid tablets.", 72, 0, 300, "39.99"),
            med("Antacid", "Eno", "5", "g", "Powder", "GSK", "Effervescent antacid powder for occasional indigestion.", 40, 0, 285, "32.50"),
            med("Antacid", "Maalox", "400", "mg", "Tablet", "Sanofi", "Aluminium and magnesium antacid chewable tablets.", 24, 24, 315, "67.00"),
            med("Antacid", "Omeprazole Cipla", "20", "mg", "Capsule", "Cipla", "Omeprazole capsules for acid-related symptoms.", 56, 0, 290, "29.00"),

            // Antidiabetic
            med("Antidiabetic", "Glucophage", "500", "mg", "Tablet", "Merck", "Metformin tablets for management of type 2 diabetes.", 120, 0, 300, "27.50"),
            med("Antidiabetic", "Metformin XR", "1000", "mg", "Tablet", "Sandoz", "Extended-release metformin tablets.", 90, 0, 280, "42.00"),
            med("Antidiabetic", "Januvia", "100", "mg", "Tablet", "MSD", "Sitagliptin tablets for prescribed diabetes management.", 30, 30, 330, "390.00"),
            med("Antidiabetic", "Diamicron MR", "60", "mg", "Tablet", "Servier", "Modified-release gliclazide tablets.", 40, 0, 295, "156.00"),
            med("Antidiabetic", "Jardiance", "10", "mg", "Tablet", "Boehringer Ingelheim", "Empagliflozin tablets for type 2 diabetes management.", 6, 0, 250, "310.00"),

            // Antihypertensive
            med("Antihypertensive", "Amlodipine Aspen", "5", "mg", "Tablet", "Aspen Pharmacare", "Amlodipine tablets for blood pressure management.", 90, 0, 310, "18.00"),
            med("Antihypertensive", "Coversyl", "5", "mg", "Tablet", "Servier", "Perindopril tablets for hypertension.", 60, 0, 280, "99.00"),
            med("Antihypertensive", "Cozaar", "50", "mg", "Tablet", "MSD", "Losartan tablets for hypertension.", 48, 48, 340, "125.00"),
            med("Antihypertensive", "Concor", "5", "mg", "Tablet", "Merck", "Bisoprolol tablets for cardiovascular conditions.", 36, 0, 300, "82.00"),
            med("Antihypertensive", "Enalapril Cipla", "10", "mg", "Tablet", "Cipla", "Enalapril tablets for blood pressure management.", 60, 0, 270, "22.50"),

            // Antiviral
            med("Antiviral", "Aciclovir Aspen", "400", "mg", "Tablet", "Aspen Pharmacare", "Aciclovir tablets for clinician-directed viral treatment.", 30, 0, 290, "58.00"),
            med("Antiviral", "Zovirax Cream", "5%", "OTHER", "Cream", "GSK", "Aciclovir cream for cold sore treatment.", 24, 0, 300, "115.00"),
            med("Antiviral", "Tamiflu", "75", "mg", "Capsule", "Roche", "Oseltamivir capsules for prescribed influenza treatment.", 20, 20, 320, "280.00"),
            med("Antiviral", "Valaciclovir Cipla", "500", "mg", "Tablet", "Cipla", "Valaciclovir tablets for clinician-directed antiviral therapy.", 30, 0, 280, "96.00"),
            med("Antiviral", "Famvir", "250", "mg", "Tablet", "Novartis", "Famciclovir tablets for prescribed herpes virus treatment.", 24, 0, 310, "205.00"),

            // Vitamin
            med("Vitamin", "Vitamin C 500", "500", "mg", "Tablet", "Clicks Group", "Vitamin C tablets for dietary supplementation.", 100, 0, 300, "24.99"),
            med("Vitamin", "Vitamin D3", "1000", "IU", "Capsule", "Solgar", "Vitamin D3 capsules for daily supplementation.", 60, 0, 280, "89.00"),
            med("Vitamin", "Vitamin B Complex", "50", "mg", "Tablet", "Nativa", "B-complex tablets for dietary supplementation.", 40, 40, 330, "69.00"),
            med("Vitamin", "Folic Acid", "5", "mg", "Tablet", "Adcock Ingram", "Folic acid tablets for clinician-recommended supplementation.", 50, 0, 290, "12.00"),
            med("Vitamin", "Ferrous Sulphate", "200", "mg", "Tablet", "Aspen Pharmacare", "Iron tablets for iron supplementation when indicated.", 48, 0, 300, "16.50"),

            // Vaccine
            med("Vaccine", "Influvac Tetra", "0.5", "ml", "Injection", "Abbott", "Quadrivalent seasonal influenza vaccine.", 12, 0, 330, "145.00"),
            med("Vaccine", "Prevenar 13", "0.5", "ml", "Injection", "Pfizer", "Pneumococcal conjugate vaccine for eligible patients.", 10, 10, 360, "770.00"),
            med("Vaccine", "Boostrix", "0.5", "ml", "Injection", "GSK", "Tetanus, diphtheria and pertussis booster vaccine.", 12, 0, 340, "340.00"),
            med("Vaccine", "Engerix-B", "20", "mcg", "Injection", "GSK", "Hepatitis B vaccine for eligible patients.", 12, 0, 320, "310.00"),
            med("Vaccine", "Gardasil 9", "0.5", "ml", "Injection", "MSD", "Human papillomavirus vaccine for eligible patients.", 12, 0, 360, "1990.00")
    );

    private final MedicationJpaRepository medicationRepository;
    private final InventoryJpaRepository inventoryRepository;
    private final MedicationService medicationService;
    private final InventoryService inventoryService;
    private final boolean enabled;

    public MedicationDataSeeder(MedicationJpaRepository medicationRepository,
                                InventoryJpaRepository inventoryRepository,
                                MedicationService medicationService,
                                InventoryService inventoryService,
                                @Value("${app.seed.sample-medications:true}") boolean enabled) {
        this.medicationRepository = medicationRepository;
        this.inventoryRepository = inventoryRepository;
        this.medicationService = medicationService;
        this.inventoryService = inventoryService;
        this.enabled = enabled;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            logger.info("Sample medication seed disabled");
            return;
        }

        int medicinesAdded = 0;
        int batchesAdded = 0;
        int medicinesFailed = 0;
        int batchesFailed = 0;
        LocalDate today = LocalDate.now();
        for (int index = 0; index < SAMPLE_MEDICATIONS.size(); index++) {
            MedicationSeed sample = SAMPLE_MEDICATIONS.get(index);
            String baseBatchNumber = "MEDSYNC-SAMPLE-" + String.format("%03d", index + 1);
            za.ac.cput.medisnyc.domain.Medication medication;
            try {
                if (medicationRepository.existsDuplicate(sample.name(), sample.strength(),
                        sample.dosageForm(), null)) {
                    continue;
                }
                if (inventoryRepository.existsByBatchNumberIgnoreCase(baseBatchNumber + "-A")
                        || (sample.secondBatchStock() > 0
                        && inventoryRepository.existsByBatchNumberIgnoreCase(baseBatchNumber + "-B"))) {
                    logger.warn("Skipping sample medicine {}; a sample batch number is already in use", sample.name());
                    continue;
                }
                medication = medicationService.addMedicine(new CreateMedicationRequest(
                        sample.name(), sample.strength(), sample.dosageForm(), sample.category(),
                        sample.manufacturer(), sample.description()), "system");
                medicinesAdded++;
            } catch (RuntimeException exception) {
                medicinesFailed++;
                logger.error("Failed to seed sample medicine {}", sample.name(), exception);
                continue;
            }

            // Demo stock expires in 2028 so the seeded catalogue stays usable.
            LocalDate firstExpiry = LocalDate.of(2028, 12, 31);
            LocalDate secondExpiry = LocalDate.of(2028, 6, 30);
            int firstBatchResult = receiveBatchSafely(medication.getMedicationId(), baseBatchNumber + "-A",
                    sample.firstBatchStock(), sample, firstExpiry, today);
            if (firstBatchResult < 0) {
                batchesFailed++;
            } else {
                batchesAdded += firstBatchResult;
            }
            if (sample.secondBatchStock() > 0) {
                int secondBatchResult = receiveBatchSafely(medication.getMedicationId(), baseBatchNumber + "-B",
                        sample.secondBatchStock(), sample, secondExpiry, today);
                if (secondBatchResult < 0) {
                    batchesFailed++;
                } else {
                    batchesAdded += secondBatchResult;
                }
            }
        }

        logger.info("Sample seeding complete: medicines added={}, failed={}; batches added={}, failed={}",
                medicinesAdded, medicinesFailed, batchesAdded, batchesFailed);
    }

    private int receiveBatchSafely(String medicationId, String batchNumber, int stock, MedicationSeed sample,
                                   LocalDate expiryDate, LocalDate receivedDate) {
        try {
            return receiveBatch(medicationId, batchNumber, stock, sample, expiryDate, receivedDate);
        } catch (RuntimeException exception) {
            logger.error("Failed to seed inventory batch {} for medicine {}", batchNumber, sample.name(), exception);
            return -1;
        }
    }

    private int receiveBatch(String medicationId, String batchNumber, int stock, MedicationSeed sample,
                             LocalDate expiryDate, LocalDate receivedDate) {
        if (inventoryRepository.existsByBatchNumberIgnoreCase(batchNumber)) {
            return 0;
        }
        inventoryService.receiveStock(new ReceiveStockRequest(
                medicationId, stock, expiryDate, batchNumber,
                SUPPLIERS[Math.floorMod(batchNumber.hashCode(), SUPPLIERS.length)],
                new BigDecimal(sample.unitPrice()), receivedDate, 10), "system");
        return 1;
    }

    static List<MedicationSeed> sampleMedications() {
        return SAMPLE_MEDICATIONS;
    }

    private static MedicationSeed med(String category, String name, String amount, String unit,
                                      String dosageForm, String manufacturer, String description,
                                      int firstBatchStock, int secondBatchStock, int expiryInDays,
                                      String unitPrice) {
        return new MedicationSeed(category, name, unit.equals("OTHER") ? amount : amount + " " + unit, unit, dosageForm,
                manufacturer, description, firstBatchStock, secondBatchStock, expiryInDays, unitPrice);
    }

    record MedicationSeed(String category, String name, String strength, String strengthUnit,
                          String dosageForm, String manufacturer, String description,
                          int firstBatchStock, int secondBatchStock, int expiryInDays,
                          String unitPrice) {
    }
}
