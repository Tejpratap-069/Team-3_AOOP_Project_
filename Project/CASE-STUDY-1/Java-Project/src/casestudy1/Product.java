package casestudy1;

import java.util.Objects;

// Product is an ENTITY. It is identified by its SKU.
// Its name and price can change with time, but it stays the same product.
public class Product {

    private final String sku;
    private String name;
    private Money price;

    public Product(String sku, String name, Money price) {
        this.sku = Objects.requireNonNull(sku);
        this.name = Objects.requireNonNull(name);
        this.price = Objects.requireNonNull(price);
    }

    public String sku() {
        return sku;
    }

    public String name() {
        return name;
    }

    public Money price() {
        return price;
    }

    public void setPrice(Money newPrice) {
        this.price = Objects.requireNonNull(newPrice);
    }
}
