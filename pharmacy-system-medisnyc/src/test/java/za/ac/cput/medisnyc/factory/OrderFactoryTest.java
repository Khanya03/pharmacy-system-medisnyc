package za.ac.cput.medisnyc.factory;
/* OrderFactoryTest.java
   Order factory Test class
   Author: Phemelo Molefi (230255299)
   Date: 20 March 2026
*/


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import za.ac.cput.domain.Order;
import za.ac.cput.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class OrderFactoryTest {

    private static Order order1;

    @BeforeClass
    public static void setUp() {
        System.out.println("============================= ORDER FACTORY TESTS ==============================");
    }

    @Test
    public void testCreateOrder_Success() {
        System.out.println("Test 1: Create Order - Success");

        order1 = OrderFactory.createOrder(
                "ORD001",
                "MED001",
                "PRES001",
                null,
                null,
                new BigDecimal("150.00"),
                "PHARM001",
                "Urgent order",
                new ArrayList<>()
        );

        assertNotNull(order1);
        assertEquals("ORD001", order1.getOrderId());
        assertEquals("MED001", order1.getPatientId());
        assertEquals(OrderStatus.PENDING, order1.getDispensingStatus());
        assertTrue(order1.canBeCancelled());

        System.out.println("✓ Order created: " + order1);
    }

    @Test
    public void testCreateOrder_NullOrderId() {
        System.out.println("Test 2: Create Order - Null ID");

        try {
            OrderFactory.createOrder(
                    null,
                    "MED001",
                    "PRES001",
                    new ArrayList<>()
            );
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Order ID cannot be null or empty", e.getMessage());
            System.out.println("✓ Correctly threw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCreateOrder_DefaultValues() {
        System.out.println("Test 3: Create Order - Default Values");

        Order simpleOrder = OrderFactory.createOrder(
                "ORD002",
                "MED002",
                "PRES002",
                new ArrayList<>()
        );

        assertNotNull(simpleOrder);
        assertEquals(OrderStatus.PENDING, simpleOrder.getDispensingStatus());
        assertEquals(BigDecimal.ZERO, simpleOrder.getTotalAmount());

        System.out.println("✓ Order with defaults created: " + simpleOrder);
    }

    @AfterClass
    public static void tearDown() {
        System.out.println("============================= ORDER FACTORY TESTS COMPLETED ===================\n");
    }
}
