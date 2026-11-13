package casestudy1;

// Demo program for Case Study 1 (Money and Pricing).
// It shows the normal working of the model and also shows that every
// wrong operation from the case study is rejected.
public class Main {

    public static void main(String[] args) {

        System.out.println("---- Normal use ----");
        Product book = new Product("SKU-1", "Java Book", Money.of("499.00", Currency.EUR));
        Product pen = new Product("SKU-2", "Pen", Money.of("9.99", Currency.EUR));

        Cart cart = new Cart("CART-1");
        cart.addItem(book, Quantity.of(1));
        cart.addItem(pen, Quantity.of(2));
        cart.addItem(pen, Quantity.of(1));   // same product again -> quantity becomes 3

        for (CartLine line : cart.lines()) {
            System.out.println(line);
        }
        System.out.println("Cart total: " + cart.total(Currency.EUR));

        System.out.println();
        System.out.println("---- Discount ----");
        Money price = Money.of("9.99", Currency.EUR);
        System.out.println("Price " + price + " after 15% discount = "
                + Percentage.ofPercent(15).applyTo(price));
        System.out.println("Price " + Money.of("10.00", Currency.EUR) + " after 25% discount = "
                + Percentage.ofPercent(25).applyTo(Money.of("10.00", Currency.EUR)));

        System.out.println();
        System.out.println("---- Other currencies ----");
        System.out.println("JPY price (no minor units): " + Money.of("500", Currency.JPY));
        System.out.println("KWD price (3 minor units): " + Money.of("1.250", Currency.KWD));

        System.out.println();
        System.out.println("---- Wrong operations are rejected ----");
        tryIt("negative price", () -> Money.of("-5.00", Currency.EUR));
        tryIt("too many decimals for JPY", () -> Money.of("10.5", Currency.JPY));
        tryIt("adding EUR and USD", () -> Money.of("1.00", Currency.EUR).plus(Money.of("1.00", Currency.USD)));
        tryIt("discount of 250%", () -> Percentage.ofPercent(250));
        tryIt("discount of -20%", () -> Percentage.ofPercent(-20));
        tryIt("quantity zero", () -> Quantity.of(0));
        tryIt("quantity negative", () -> Quantity.of(-3));
    }

    private static void tryIt(String what, Runnable action) {
        try {
            action.run();
            System.out.println(what + " -> NOT rejected (problem!)");
        } catch (RuntimeException e) {
            System.out.println(what + " -> rejected: " + e.getMessage());
        }
    }
}
