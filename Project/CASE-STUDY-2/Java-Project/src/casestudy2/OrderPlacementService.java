package casestudy2;

// Service which places an order. The credit limit rule is a
// CROSS-AGGREGATE rule: one Order object cannot check it, because the
// rule needs the customer's limit and the credit already used by the
// customer's OTHER orders. So the rule lives here, not inside Order.
//
// The check and the reservation are done together in one synchronized
// method: first check the available credit, then reserve the amount,
// then place the order. If two orders of the same customer are placed
// at the same time, they cannot both pass the check with the same
// credit, because the second one will see the first one's reservation.
// (With a real database, the same thing is done with one guarded
// UPDATE ... WHERE reserved + amount <= limit statement.)
public class OrderPlacementService {

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
}
