package za.ac.cput.medisnyc.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(5)
public class InventoryMinimumStockMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(InventoryMinimumStockMigration.class);
    private static final int MINIMUM_STOCK = 10;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        int updated = entityManager.createQuery(
                        "update Inventory i set i.stockLevel = :minimum where i.stockLevel < :minimum")
                .setParameter("minimum", MINIMUM_STOCK)
                .executeUpdate();
        if (updated > 0) {
            logger.info("Raised {} inventory batch(es) to at least {} units", updated, MINIMUM_STOCK);
        }
    }
}
