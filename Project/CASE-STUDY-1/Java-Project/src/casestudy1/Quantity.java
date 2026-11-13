package casestudy1;

// Quantity is a VALUE OBJECT. It is always a positive whole number (1 or more).
// Zero and negative quantities can never be created.
public final class Quantity {

    private final int value;

    private Quantity(int value) {
        this.value = value;
    }

    public static Quantity of(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1: " + value);
        }
        return new Quantity(value);
    }

    public Quantity add(Quantity other) {
        return new Quantity(Math.addExact(value, other.value));
    }

    public int value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Quantity q && value == q.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
