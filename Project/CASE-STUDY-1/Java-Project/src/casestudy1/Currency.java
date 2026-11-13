package casestudy1;

// Currency is a fixed set of values, not a free String.
// Each currency also tells how many decimal digits (minor units) it has.
// EUR/USD: 100 minor units = 1 unit, JPY: no minor units, KWD: 1000 = 1 unit.
public enum Currency {
    EUR(2), USD(2), JPY(0), KWD(3);

    final int minorUnits;

    Currency(int minorUnits) {
        this.minorUnits = minorUnits;
    }
}
