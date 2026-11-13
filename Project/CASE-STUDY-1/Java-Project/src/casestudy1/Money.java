package casestudy1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

// Money is a VALUE OBJECT. The amount is stored as a long count of
// minor units (for example 1050 = EUR 10.50), never as a double,
// so there are no floating point rounding errors.
// A Money object can never be negative and can never mix currencies.
public final class Money {

    private final long minor;          // amount in minor units
    private final Currency currency;

    private Money(long minor, Currency currency) {
        this.minor = minor;
        this.currency = currency;
    }

    // Creates Money from a decimal string like "10.50".
    // Rejects negative amounts and too many decimal places for the currency.
    public static Money of(String decimal, Currency currency) {
        Objects.requireNonNull(currency);
        BigDecimal value = new BigDecimal(decimal);
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Money cannot be negative: " + decimal);
        }
        // shift decimal point by the currency's minor units, must become a whole number
        BigDecimal inMinor = value.movePointRight(currency.minorUnits);
        if (inMinor.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException(
                    "Too many decimal places for " + currency + ": " + decimal);
        }
        return new Money(inMinor.longValueExact(), currency);
    }

    public static Money ofMinorUnits(long minor, Currency currency) {
        if (minor < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
        return new Money(minor, Objects.requireNonNull(currency));
    }

    public static Money zero(Currency currency) {
        return new Money(0, currency);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(Math.addExact(minor, other.minor), currency);
    }

    // Price after removing a percentage discount.
    // Rounding is done only here, with one fixed rule: HALF_EVEN.
    public Money minus(Percentage discount) {
        long discountAmount = BigDecimal.valueOf(minor)
                .multiply(BigDecimal.valueOf(discount.basisPoints()))
                .divide(BigDecimal.valueOf(10_000), 0, RoundingMode.HALF_EVEN)
                .longValueExact();
        return new Money(minor - discountAmount, currency);
    }

    public Money times(Quantity quantity) {
        return new Money(Math.multiplyExact(minor, quantity.value()), currency);
    }

    private void requireSameCurrency(Money other) {
        if (currency != other.currency) {
            throw new IllegalArgumentException(
                    "Currency mismatch: cannot combine " + currency + " with " + other.currency);
        }
    }

    public long minorUnits() {
        return minor;
    }

    public Currency currency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Money m && minor == m.minor && currency == m.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minor, currency);
    }

    @Override
    public String toString() {
        BigDecimal v = BigDecimal.valueOf(minor).movePointLeft(currency.minorUnits);
        return currency + " " + v.toPlainString();
    }
}
