package casestudy1;

import java.util.Objects;

// Percentage is a VALUE OBJECT stored in basis points:
// 100% = 10000 basis points, 25% = 2500 basis points.
// The constructor is private, so an object can only be created through
// the factory methods below, and they check the range 0 to 100 first.
// That means a Percentage of -20% or 250% can never be created.
public final class Percentage {

    private final int basisPoints;   // 0..10000

    private Percentage(int basisPoints) {
        this.basisPoints = basisPoints;
    }

    public static Percentage ofBasisPoints(int bp) {
        if (bp < 0 || bp > 10_000) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100: " + bp + " bp");
        }
        return new Percentage(bp);
    }

    public static Percentage ofPercent(int percent) {
        return ofBasisPoints(Math.multiplyExact(percent, 100));
    }

    public int basisPoints() {
        return basisPoints;
    }

    // Gives the price after this discount percentage is removed.
    public Money applyTo(Money price) {
        Objects.requireNonNull(price);
        return price.minus(this);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Percentage p && basisPoints == p.basisPoints;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(basisPoints);
    }

    @Override
    public String toString() {
        return (basisPoints / 100) + "%";
    }
}
