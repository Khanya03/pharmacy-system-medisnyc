package za.ac.cput.medisnyc.repository;

/* OrderRepositoryTest.java
   Order repository Test class
   Author: Phemelo Molefi (230255299)
   Date: 23 March 2026
*/

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import za.ac.cput.medisnyc.domain.Order;
import za.ac.cput.medisnyc.domain.OrderStatus;
import za.ac.cput.medisnyc.factory.OrderFactory;
import za.ac.cput.medisnyc.repository.impl.OrderRepositoryImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class OrderRepositoryTest {

    private static OrderRepository repository;
    private static Order order1;
    private static Order order2;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("============================= ORDER REPOSITORY TESTS ===========================");
        repository = OrderRepositoryImpl.getRepository();
    }

    @Before
    public void setUp() {
        // Create test orders before each test
        order1 = OrderFactory.createOrder(
                "ORD001", "PAT001", "PRES001",
                null, OrderStatus.PENDING, new BigDecimal("150.00"),
                "PHARM001", null, new ArrayList<>());

        order2 = OrderFactory.createOrder(
                "ORD002", "PAT002", "PRES002",
                null, OrderStatus.DISPENSED, new BigDecimal("75.50"),
                "PHARM002", null, new ArrayList<>());
    }

    @Test
    public void testCreate() {
        System.out.println("Test 1: Create Order");

        Order created = repository.create(order1);

        assertNotNull(created);
        assertEquals("ORD001", created.getOrderId());

        System.out.println("✓ Created: " + created);
    }

    @Test
    public void testRead() {
        System.out.println("Test 2: Read Order");

        repository.create(order1);

        // Now read() returns T directly, not Optional<T>
        Order read = repository.read("ORD001");

        assertNotNull(read);  // Check not null instead of isPresent()
        assertEquals("PAT001", read.getPatientId());

        System.out.println("✓ Read: " + read);
    }

    @Test
    public void testReadNotFound() {
        System.out.println("Test 2b: Read Order Not Found");

        // Test when order doesn't exist - should return null
        Order read = repository.read("NONEXISTENT");

        assertNull(read);  // Should be null when not found

        System.out.println("✓ Correctly returned null for non-existent order");
    }

    @Test
    public void testFindByPatientId() {
        System.out.println("Test 3: Find By Patient ID");

        repository.create(order1);
        repository.create(order2);

        List<Order> results = repository.findByPatientId("PAT001");

        assertEquals(1, results.size());
        assertEquals("ORD001", results.get(0).getOrderId());

        System.out.println("✓ Found " + results.size() + " order(s) for patient");
    }

    @Test
    public void testFindByStatus() {
        System.out.println("Test 4: Find By Status");

        repository.create(order1);  // PENDING
        repository.create(order2);  // DISPENSED

        List<Order> pending = repository.findByStatus(OrderStatus.PENDING);

        assertEquals(1, pending.size());
        assertEquals("ORD001", pending.get(0).getOrderId());

        System.out.println("✓ Found " + pending.size() + " pending order(s)");
    }

    @Test
    public void testFindPendingOrders() {
        System.out.println("Test 5: Find Pending Orders");

        repository.create(order1);  // PENDING
        repository.create(order2);  // DISPENSED

        List<Order> pending = repository.findPendingOrders();

        assertEquals(1, pending.size());
        assertEquals("ORD001", pending.get(0).getOrderId());

        System.out.println("✓ Found " + pending.size() + " pending order(s)");
    }

    @Test
    public void testFindOrdersBetweenDates() {
        System.out.println("Test 6: Find Orders Between Dates");

        repository.create(order1);
        repository.create(order2);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        List<Order> results = repository.findOrdersBetweenDates(start, end);

        assertEquals(2, results.size());

        System.out.println("✓ Found " + results.size() + " order(s) in date range");
    }

    @Test
    public void testUpdate() {
        System.out.println("Test 7: Update Order");

        repository.create(order1);

        Order updated = OrderFactory.createOrder(
                "ORD001", "PAT001", "PRES001",
                null, OrderStatus.PROCESSING, new BigDecimal("150.00"),
                "PHARM001", "Updated notes", new ArrayList<>());

        Order result = repository.update(updated);

        assertNotNull(result);
        assertEquals(OrderStatus.PROCESSING, result.getDispensingStatus());
        assertEquals("Updated notes", result.getNotes());

        System.out.println("✓ Updated: " + result);
    }

    @Test
    public void testDelete() {
        System.out.println("Test 8: Delete Order");

        repository.create(order1);

        boolean deleted = repository.delete("ORD001");

        assertTrue(deleted);

        // Verify it's gone
        Order read = repository.read("ORD001");
        assertNull(read);

        System.out.println("✓ Deleted successfully");
    }

    @Test
    public void testGetAll() {
        System.out.println("Test 9: Get All Orders");

        repository.create(order1);
        repository.create(order2);

        List<Order> all = repository.getAll();

        assertEquals(2, all.size());

        System.out.println("✓ Found " + all.size() + " total order(s)");
    }
}