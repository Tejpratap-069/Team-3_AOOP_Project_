package casestudy1;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// One line inside a cart: a product and how many pieces.
// If the same product is added again, the quantities are combined.
class CartLine {
    private final Product product;
    private Quantity quantity;

    CartLine(Product product, Quantity quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    void addQuantity(Quantity more) {
        this.quantity = this.quantity.add(more);
    }

    Money lineTotal() {
        return product.price().times(quantity);
    }

    @Override
    public String toString() {
        return product.name() + " x" + quantity + " = " + lineTotal();
    }
}

// Cart is an ENTITY and the aggregate root. It is identified by its cart id.
// The total is always CALCULATED from the lines, never stored separately,
// so the total can never become different from the lines.
public class Cart {

    private final String cartId;
    private final Map<String, CartLine> lines = new LinkedHashMap<>();

    public Cart(String cartId) {
        this.cartId = Objects.requireNonNull(cartId);
    }

    public void addItem(Product product, Quantity quantity) {
        CartLine existing = lines.get(product.sku());
        if (existing == null) {
            lines.put(product.sku(), new CartLine(product, quantity));
        } else {
            existing.addQuantity(quantity);
        }
    }

    public Money total(Currency currency) {
        Money sum = Money.zero(currency);
        for (CartLine line : lines.values()) {
            sum = sum.plus(line.lineTotal());
        }
        return sum;
    }

    public List<CartLine> lines() {
        return new ArrayList<>(lines.values());
    }

    public String cartId() {
        return cartId;
    }
}
