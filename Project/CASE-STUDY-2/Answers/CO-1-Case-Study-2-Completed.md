# CO-1 Case Study 2 — Completed Answers

## Problems Identified

**Problems we identified in the given (naive) model:**

- The state is kept in a `String` status plus separate boolean flags like paid, shipped and cancelled. Opposite states can come together, like paid = true with status = "DRAFT", or shipped = true and cancelled = true at the same time.
- `shippedAt` and the tracking number are separate nullable fields, so they can disagree with the state — a SHIPPED order can have `shippedAt` as null, and a draft order can have a tracking number.
- The list of lines is public, so any code can change the lines without the total coming to know, and the total becomes wrong.
- Any code in the program can directly write `order.status = "SHIPPED"` and skip the payment step fully.
- An order with no lines at all can also be placed, because nobody checks it.

## Question 1

**Answer:**

In the naive model the following illegal or contradictory states can easily come:

1. **status = "PAID" but paid = false** (or paid = true with status = "DRAFT"). The string and the boolean are telling two different things about the same order. This breaks the rule that the state of the order is one single fact.
2. **shipped = true and cancelled = true at the same time.** An order cannot be both shipped and cancelled. This breaks the lifecycle rule.
3. **status = "SHIPPED" but shippedAt = null and tracking number = null.** The order is shown shipped but there is no proof of shipping. This breaks the rule that shipped details are present if and only if the order is shipped.
4. **status = "DRAFT" but a tracking number and shippedAt are present.** The order is not even placed, still it has shipment data. This is the same rule broken from the other side.
5. **An order is placed or paid with zero lines.** This breaks the rule that a placed order has at least one line.
6. **status = "DELIVERED" for an order which was never paid**, because somebody directly set the status string. This breaks the fixed order of transitions — payment cannot be skipped.

## Question 2

**Answer:**

The state transition table of the order lifecycle is:

| From state | Action | To state | Condition |
|---|---|---|---|
| Draft | place() | Placed | Order has at least one line |
| Placed | pay() | Paid | - |
| Paid | ship(trackingNumber) | Shipped | Tracking number is given |
| Shipped | deliver() | Delivered | - |
| Draft | cancel() | Cancelled | - |
| Placed | cancel() | Cancelled | - |
| Paid | cancel() | Cancelled | Refund is handled separately |
| Shipped | cancel() | Not allowed | - |
| Delivered | cancel() | Not allowed | - |
| Delivered / Cancelled | any action | Not allowed | These are final states |

The code for the ship transition, from our Order class (Model A). The state is checked first, and only after that the shipment is created and the state is changed:

```java
public void ship(String trackingNumber) {
    requireStatus(Status.PAID, "ship");
    // tracking details and shipped time are created together, here only
    this.shipment = new Shipment(trackingNumber, Instant.now());
    status = Status.SHIPPED;
}

private void requireStatus(Status expected, String action) {
    if (status != expected) {
        throw new DomainException(
                "Cannot " + action + " when the order is in state "
                        + status + " (needs " + expected + ")");
    }
}
```

Why this is safe: `ship()` works only when the order is PAID, so shipping before payment is not possible. The `Shipment` object (tracking number + shipped time together) is created at this one place only, so a shipped order always has tracking details and a non-shipped order never has them. There is also no way to cancel after this, because `cancel()` rejects the SHIPPED state.

## Question 3

**Answer:**

| Point | Model A (one class, guarded state machine) | Model B (one class per state) |
|---|---|---|
| Type safety | Less. All methods are present on the one Order class, so a wrong call like ship() on a draft can be written; it is caught at run time when the method checks the state and throws an error. | More. A DraftOrder does not even have a ship() method, so a wrong call cannot be written at all; it is caught by the compiler. |
| Code volume | Less code. Only one Order class, state kept in one Status field. | Much more code. One class per state (DraftOrder, PlacedOrder, PaidOrder, ShippedOrder, CancelledOrder) and every transition creates a new object of the next class. |
| Persistence (saving in database) | Easy. One row with a status column; reading gives back the same Order object. | Difficult. We have to save which type the order is in, and on reading we must create the object of that state class. Changing one order also means replacing the object. |

**Our recommendation:** for the given situation we select **Model A**. Here the number of states is more (around 8 with delivery and cancellation) and business rules keep changing, like new states or new cancellation rules. In Model B every rule change will touch many classes and the saving/loading will become complicated. Model A gives almost the same protection with much less code — all fields are private and every method checks the state before changing it. If the states were few and fully fixed, and compile-time safety was the most important thing, then Model B would be the better choice.

## Question 4

**Answer:**

The total is better **computed from the lines** every time, because then there is only one source of truth — the lines. If the total is stored in a separate field, it has to be updated at every place where lines change, and if even one place is missed, the total and the lines will differ and the customer will be charged a wrong amount. When it is computed, the total can never go wrong, because it is made from the lines only at that time.

Still, storing the total can be justified in two situations:

- **Performance:** if an order has a very large number of lines and the total is read again and again (like on every page of the shop), calculating it every time can be slow. Then a stored total works like a cache.
- **Freezing the charged amount:** at payment time the amount charged should not change later even if product prices change. So at the time of payment we may save the charged total with the payment record.

If the total is stored, one extra invariant comes: **storedTotal must always be equal to the sum of the line totals**. It must be updated in the same work as the lines change, otherwise the same old problem returns.

## Question 5

**Answer:**

No, the credit limit is **not an invariant of one Order object**. A single order only knows its own total. The rule talks about the customer's limit and the totals of all the customer's other orders also, which one Order object cannot see. So this rule cannot be written inside the Order class.

This rule is a cross-aggregate rule, so we keep it in a service which places the order (in our code, `OrderPlacementService`):

```java
public synchronized void placeOrder(Order order, Customer customer) {
    Money total = order.total();
    if (total.compareTo(customer.availableCredit()) > 0) {
        throw new DomainException("Order total " + total
                + " is more than the available credit " + customer.availableCredit()
                + " of customer " + customer.name());
    }
    customer.reserve(total);
    order.place();
}
```

Here the checking and reserving of credit is done together in one synchronized method. This point is important: if two orders of the same customer are placed at the same time, and we only check the credit without reserving, both orders will see enough credit, both will be placed, and together they will cross the limit. That is the race condition. When the first order reserves its amount immediately after checking, the second order sees less available credit and gets rejected.

At the database level also, the same protection should be there: either lock the customer's credit row while placing, or do one guarded update like `UPDATE customer_credit SET reserved = reserved + :amount WHERE customer_id = :id AND reserved + :amount <= :limit` and check that one row was actually updated. Then even if two requests come at the same time, the database itself will not allow the limit to cross.
