package casestudy2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Order is an ENTITY and the aggregate root (Model A: one class with a
// guarded state machine). All fields are private, so the state can be
// changed only through the methods below, and every method first checks
// that the change is allowed in the present state.
//
// Allowed transitions:
//   DRAFT -> PLACED -> PAID -> SHIPPED -> DELIVERED
//   DRAFT / PLACED / PAID -> CANCELLED
//   DELIVERED and CANCELLED are final states.
public class Order {

    private final String orderId;
    private final Map<String, OrderLine> lines = new LinkedHashMap<>();
    private Status status = Status.DRAFT;
    private Shipment shipment;   // null until the order is shipped

    public Order(String orderId) {
        this.orderId = orderId;
    }

    // Lines can be added or removed only while the order is a draft.
    // Adding the same product again increases its quantity (lines are
    // unique by product).
    public void addLine(OrderLine line) {
        requireStatus(Status.DRAFT, "add lines");
        OrderLine existing = lines.get(line.sku());
        if (existing == null) {
            lines.put(line.sku(), line);
        } else {
            lines.put(line.sku(), existing.withAddedQuantity(line.quantity()));
        }
    }

    public void removeLine(String sku) {
        requireStatus(Status.DRAFT, "remove lines");
        lines.remove(sku);
    }

    // The total is always CALCULATED from the lines, never stored,
    // so it can never become different from the lines.
    public Money total() {
        Money sum = Money.zero();
        for (OrderLine line : lines.values()) {
            sum = sum.plus(line.lineTotal());
        }
        return sum;
    }

    public void place() {
        requireStatus(Status.DRAFT, "place");
        if (lines.isEmpty()) {
            throw new DomainException("An order with no lines cannot be placed");
        }
        status = Status.PLACED;
    }

    public void pay() {
        requireStatus(Status.PLACED, "pay");
        status = Status.PAID;
    }

    public void ship(String trackingNumber) {
        requireStatus(Status.PAID, "ship");
        // tracking details and shipped time are created together, here only
        this.shipment = new Shipment(trackingNumber, Instant.now());
        status = Status.SHIPPED;
    }

    public void deliver() {
        requireStatus(Status.SHIPPED, "deliver");
        status = Status.DELIVERED;
    }

    public void cancel() {
        if (status == Status.SHIPPED || status == Status.DELIVERED
                || status == Status.CANCELLED) {
            throw new DomainException("Order in state " + status + " cannot be cancelled");
        }
        status = Status.CANCELLED;
    }

    private void requireStatus(Status expected, String action) {
        if (status != expected) {
            throw new DomainException(
                    "Cannot " + action + " when the order is in state " + status + " (needs " + expected + ")");
        }
    }

    public String orderId() {
        return orderId;
    }

    public Status status() {
        return status;
    }

    public List<OrderLine> lines() {
        return new ArrayList<>(lines.values());
    }

    public Optional<Shipment> shipment() {
        return Optional.ofNullable(shipment);
    }
}
