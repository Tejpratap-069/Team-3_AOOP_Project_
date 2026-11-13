package casestudy2;

// Demo program for Case Study 2 (Order Lifecycle).
// It shows one full normal lifecycle of an order and also shows that
// every wrong operation from the case study is rejected.
public class Main {

    private static int checks = 0;

    public static void main(String[] args) {

        System.out.println("---- Normal lifecycle: DRAFT -> PLACED -> PAID -> SHIPPED -> DELIVERED ----");
        Order order = new Order("ORD-1");
        order.addLine(new OrderLine("SKU-1", "Java Book", Money.of("499.00"), 1));
        order.addLine(new OrderLine("SKU-2", "Pen", Money.of("10.00"), 2));
        order.addLine(new OrderLine("SKU-2", "Pen", Money.of("10.00"), 1)); // same product again
        ok(order.lines().size() == 2, "same product added again merges into one line (quantity 3)");
        System.out.println("Lines: " + order.lines());
        System.out.println("Total (calculated from lines): " + order.total());
        ok(order.total().equals(Money.of("529.00")), "total = 499 + 3 x 10 = EUR 529.00");

        order.place();
        System.out.println("After place(): " + order.status());
        order.pay();
        System.out.println("After pay():   " + order.status());
        order.ship("TRK-123");
        System.out.println("After ship():  " + order.status() + ", shipment = " + order.shipment().orElse(null));
        order.deliver();
        System.out.println("After deliver(): " + order.status());

        System.out.println();
        System.out.println("---- Wrong operations are rejected ----");
        Order o2 = new Order("ORD-2");
        reject("placing an empty order", o2::place);
        o2.addLine(new OrderLine("SKU-1", "Java Book", Money.of("499.00"), 1));
        reject("shipping before payment", () -> o2.ship("TRK-9"));
        reject("delivering before shipping", o2::deliver);
        o2.place();
        reject("adding a line after the order is placed",
                () -> o2.addLine(new OrderLine("SKU-3", "Bag", Money.of("99.00"), 1)));
        reject("paying two times", () -> {
            o2.pay(); o2.pay();
        });
        // note: in the check above, the first pay() succeeded, so o2 is now PAID
        o2.ship("TRK-55");
        reject("cancelling a shipped order", o2::cancel);
        o2.deliver();
        reject("cancelling a delivered order", o2::cancel);

        Order o3 = new Order("ORD-3");
        o3.addLine(new OrderLine("SKU-1", "Java Book", Money.of("100.00"), 1));
        o3.place(); o3.pay(); o3.cancel();
        // PAID -> CANCELLED is allowed, so no check here; verify state instead:
        ok(o3.status() == Status.CANCELLED, "a paid order can still be cancelled (refund handled separately)");

        System.out.println();
        System.out.println("---- Credit limit rule (cross-aggregate) ----");
        OrderPlacementService service = new OrderPlacementService();
        Customer tej = new Customer("CUST-1", "Tej", Money.of("1000.00"));
        Order big = new Order("ORD-4");
        big.addLine(new OrderLine("SKU-9", "Laptop", Money.of("600.00"), 1));
        service.placeOrder(big, tej);
        ok(big.status() == Status.PLACED, "order of EUR 600 placed within credit limit 1000");
        System.out.println("Credit reserved now: " + tej.reserved() + ", available: " + tej.availableCredit());

        Order second = new Order("ORD-5");
        second.addLine(new OrderLine("SKU-9", "Laptop", Money.of("600.00"), 1));
        reject("second order of EUR 600 crosses the credit limit (only 400 left)",
                () -> service.placeOrder(second, tej));
        ok(second.status() == Status.DRAFT, "rejected order stays a draft and no credit is reserved");

        System.out.println();
        System.out.println("ALL DEMO CHECKS PASSED (" + checks + " checks)");
    }

    private static void ok(boolean condition, String what) {
        checks++;
        if (!condition) {
            System.out.println("  [FAIL] " + what);
            throw new AssertionError(what);
        }
        System.out.println("  [OK] " + what);
    }

    private static void reject(String what, Runnable action) {
        checks++;
        try {
            action.run();
            System.out.println("  [FAIL] " + what + " -> NOT rejected (problem!)");
            throw new AssertionError(what);
        } catch (DomainException e) {
            System.out.println("  [OK] rejected " + what + " -> " + e.getMessage());
        }
    }
}
