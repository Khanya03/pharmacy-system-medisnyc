package za.ac.cput.medisnyc.repository;

/* InventoryRepositoryTest.java
   Inventory Repository Test class
   Author: [Thakane Jeanet Moloi ] ([230186904])
   Date: 16 March 2026
*/

import org.junit.*;
import za.ac.cput.medisnyc.domain.Inventory;
import za.ac.cput.medisnyc.factory.InventoryFactory;
import za.ac.cput.medisnyc.repository.impl.InventoryRepositoryImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class InventoryRepositoryTest {

    private static InventoryRepository repository;
    private static Inventory inv1;
    private static Inventory inv2;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("============================= INVENTORY REPOSITORY TESTS =======================");
        repository = InventoryRepositoryImpl.getRepository();
    }

    @Before
    public void setUp() {
        inv1 = InventoryFactory.createInventory(
                "INV001", "MED001", 100,
                LocalDate.of(2026, 12, 31), "BATCH001",
                "Supplier A", new BigDecimal("15.99"), LocalDate.now(), 20);

        inv2 = InventoryFactory.createInventory(
                "INV002", "MED001", 5,
                LocalDate.of(2026, 6, 30), "BATCH002",
                "Supplier B", new BigDecimal("12.99"), LocalDate.now(), 10);

        // Clean up before each test
        for (Inventory i : repository.getAll()) {
            repository.delete(i.getInventoryId());
        }
    }

    @Test
    public void testCreate() {
        System.out.println("Test 1: Create Inventory");

        Inventory created = repository.create(inv1);
        assertNotNull(created);
        assertEquals("INV001", created.getInventoryId());

        System.out.println("✓ Created: " + created);
    }

    @Test
    public void testRead() {
        System.out.println("Test 2: Read Inventory");

        repository.create(inv1);
        Inventory read = repository.read("INV001");

        assertNotNull(read);
        assertEquals("MED001", read.getMedicationId());

        System.out.println("✓ Read: " + read);
    }

    @Test
    public void testFindByMedicationId() {
        System.out.println("Test 3: Find By Medication ID");

        repository.create(inv1);
        repository.create(inv2);

        List<Inventory> results = repository.findByMedicationId("MED001");
        assertEquals(2, results.size());

        System.out.println("✓ Found " + results.size() + " inventory item(s) for MED001");
    }

    @Test
    public void testFindLowStockItems() {
        System.out.println("Test 4: Find Low Stock Items");

        repository.create(inv1);
        repository.create(inv2);

        List<Inventory> lowStock = repository.findLowStockItems();
        assertEquals(1, lowStock.size());
        assertEquals("INV002", lowStock.get(0).getInventoryId());

        System.out.println("✓ Found " + lowStock.size() + " low stock item(s)");
    }

    @Test
    public void testUpdate() {
        System.out.println("Test 5: Update Inventory");

        repository.create(inv1);

        Inventory updated = InventoryFactory.createInventory(
                "INV001", "MED001", 150,
                LocalDate.of(2026, 12, 31), "BATCH001-UPDATED",
                "Supplier A Updated", new BigDecimal("17.99"), LocalDate.now(), 25);

        Inventory result = repository.update(updated);
        assertNotNull(result);
        assertEquals(150, result.getStockLevel());
        assertEquals("BATCH001-UPDATED", result.getBatchNumber());

        System.out.println("✓ Updated: " + result);
    }

    @Test
    public void testUpdateStockLevel() {
        System.out.println("Test 6: Update Stock Level");

        repository.create(inv1);
        boolean updated = repository.updateStockLevel("INV001", 75);

        assertTrue(updated);

        Inventory updatedInv = repository.read("INV001");
        assertNotNull(updatedInv);
        assertEquals(75, updatedInv.getStockLevel());

        System.out.println("✓ Stock level updated to 75");
    }

    @Test
    public void testDelete() {
        System.out.println("Test 7: Delete Inventory");

        repository.create(inv1);
        boolean deleted = repository.delete("INV001");

        assertTrue(deleted);
        assertNull(repository.read("INV001"));

        System.out.println("✓ Deleted successfully");
    }

    @Test
    public void testGetAll() {
        System.out.println("Test 8: Get All Inventory");

        repository.create(inv1);
        repository.create(inv2);

        List<Inventory> all = repository.getAll();
        assertEquals(2, all.size());

        System.out.println("✓ Retrieved " + all.size() + " inventory items");
    }

    @After
    public void cleanUp() {
        for (Inventory i : repository.getAll()) {
            repository.delete(i.getInventoryId());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("============================= INVENTORY REPOSITORY TESTS COMPLETED ============\n");
    }
}