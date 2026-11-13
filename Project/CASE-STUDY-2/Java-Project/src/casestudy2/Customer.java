package casestudy2;

// Customer is an ENTITY. For this case study we only need the part
// which is used in the credit limit rule: the customer's credit limit
// and how much credit is already used/reserved by placed orders.
public class Customer {

    private final String customerId;
    private final String name;
    private final Money creditLimit;
    private Money creditReserved = Money.zero();

    public Customer(String customerId, String name, Money creditLimit) {
        this.customerId = customerId;
        this.name = name;
        this.creditLimit = creditLimit;
    }

    public Money availableCredit() {
        // reserved is always <= limit, because it is checked at reservation time
        return creditLimit.minus(creditReserved);
    }

    Money limit() {
        return creditLimit;
    }

    Money reserved() {
        return creditReserved;
    }

    void reserve(Money amount) {
        creditReserved = creditReserved.plus(amount);
    }

    public String name() {
        return name;
    }

    public String customerId() {
        return customerId;
    }
}
