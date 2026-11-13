package casestudy2;

// One line of an order: a product, its price and the quantity.
// It is a VALUE OBJECT - it is known by its values and never changes;
// when quantity increases, a new line value replaces the old one.
public record OrderLine(String sku, String name, Money unitPrice, int quantity) {

    public OrderLine {
        if (quantity < 1) {
            throw new DomainException("Line quantity must be at least 1");
        }
    }

    public Money lineTotal() {
        return unitPrice.times(quantity);
    }

    public OrderLine withAddedQuantity(int more) {
        return new OrderLine(sku, name, unitPrice, quantity + more);
    }
}
