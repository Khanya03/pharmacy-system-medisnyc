package za.ac.cput.medisnyc.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@Order(4)
public class InventoryExpiry2028Migration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(InventoryExpiry2028Migration.class);
    private static final LocalDate TARGET = LocalDate.of(2028, 12, 31);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        int updated = entityManager.createQuery(
                        "update Inventory i set i.expiryDate = :expiry where i.expiryDate is null or i.expiryDate <> :expiry")
                .setParameter("expiry", TARGET)
                .executeUpdate();
        if (updated > 0) {
            logger.info("Updated {} inventory batch(es) to expire on {}", updated, TARGET);
        }
    }
}
