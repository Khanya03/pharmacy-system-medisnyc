package za.ac.cput.medisnyc.factory;


/* InventoryFactoryTest.java
   Inventory Factory Test class
   Author: [Thakane Jeanet Moloi] ([230186904])
   Date: 16 March 2026
*/

import org.junit.*;
import za.ac.cput.medisnyc.domain.Inventory;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.Assert.*;

public class InventoryFactoryTest {

    private static Inventory inventory1;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("============================= INVENTORY FACTORY TESTS ==========================");
    }

    @Test
    public void testCreateInventory_Success() {
        System.out.println("Test 1: Create Inventory - Success");

        inventory1 = InventoryFactory.createInventory(
                "INV001",
                "MED001",
                100,
                LocalDate.of(2026, 12, 31),
                "BATCH001",
                "Supplier A",
                new BigDecimal("15.99"),
                LocalDate.now(),
                20
        );

        assertNotNull(inventory1);
        assertEquals("INV001", inventory1.getInventoryId());
        assertEquals(100, inventory1.getStockLevel());
        assertFalse(inventory1.isLowStock());
        assertFalse(inventory1.isExpired());

        System.out.println("✓ Inventory created: " + inventory1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInventory_ExpiredDate() {
        System.out.println("Test 2: Create Inventory - Expired Date");

        InventoryFactory.createInventory(
                "INV002",
                "MED001",
                50,
                LocalDate.now().minusDays(1),
                "BATCH002",
                "Supplier B",
                new BigDecimal("10.00"),
                LocalDate.now(),
                10
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInventory_NegativeStock() {
        System.out.println("Test 3: Create Inventory - Negative Stock");

        InventoryFactory.createInventory(
                "INV003",
                "MED001",
                -10,
                LocalDate.of(2026, 12, 31),
                "BATCH003",
                "Supplier C",
                new BigDecimal("5.00"),
                LocalDate.now(),
                5
        );
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("============================= INVENTORY FACTORY TESTS COMPLETED ================\n");
    }
}