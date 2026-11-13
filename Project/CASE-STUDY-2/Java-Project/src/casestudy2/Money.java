package casestudy2;

import java.math.BigDecimal;

// Simple money value for this case study (single currency, EUR).
// Amount is stored as a long count of cents, never as double,
// so totals are always exact. (Full multi-currency Money is Case Study 1.)
public final class Money implements Comparable<Money> {

    private final long cents;

    private Money(long cents) {
        this.cents = cents;
    }

    public static Money of(String euros) {
        long c = new BigDecimal(euros).movePointRight(2).longValueExact();
        if (c < 0) {
            throw new DomainException("Money cannot be negative: " + euros);
        }
        return new Money(c);
    }

    public static Money zero() {
        return new Money(0);
    }

    public Money plus(Money other) {
        return new Money(Math.addExact(cents, other.cents));
    }

    public Money minus(Money other) {
        if (other.cents > cents) {
            throw new DomainException("Money result cannot be negative");
        }
        return new Money(cents - other.cents);
    }

    public Money times(int qty) {
        return new Money(Math.multiplyExact(cents, qty));
    }

    @Override
    public int compareTo(Money other) {
        return Long.compare(cents, other.cents);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Money m && cents == m.cents;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(cents);
    }

    @Override
    public String toString() {
        return "EUR " + BigDecimal.valueOf(cents).movePointLeft(2).toPlainString();
    }
}
