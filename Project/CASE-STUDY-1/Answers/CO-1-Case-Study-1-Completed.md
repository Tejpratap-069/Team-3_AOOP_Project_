# CO-1 Case Study 1 — Completed Answers

## Problems Identified

**Problems we identified in the given (naive) model:**

- Price is stored in a `double`, so rounding errors come in calculations and sometimes the cart total and the invoice total differ by a few cents.
- Currency is stored as a normal `String`. Any value like "usd", "US$" or even empty can be saved, nothing stops it.
- `discountPercent` is not checked anywhere. A discount of -20 or 250 is also accepted.
- Quantity can be zero or negative, which has no meaning for a cart item.
- Money of two different currencies can be added by mistake, because the code does not check the currency. The caller has to remember it every time.
- There is no fixed place where rounding is done, so different parts of the program can round in different ways and give different totals.

## Question 1

**Answer:**

`double` is not suitable for money because the computer stores decimal numbers in binary form, and most decimal values cannot be stored exactly in binary. For example, `0.1 + 0.2` does not give exactly `0.3` in Java, it gives `0.30000000000000004`. One such small error is not a problem, but in a shop there are many additions and multiplications, so the errors keep adding. In the end the total on the cart page and the total on the invoice can differ by one or two cents, like it happened in this case study.

Instead of `double`, we store the amount as a `long` in minor units. Minor unit means the smallest unit of the currency, for example cents for Euro and Dollar (100 cents = 1 Euro) and paise for Rupee. So EUR 10.50 is stored as the number 1050. All calculations on `long` are exact integer calculations, so no rounding error comes in addition and multiplication, and the cart and invoice always show the same total.

One thing we still have to decide clearly is the rounding rule. When we apply a percentage discount, the answer can come in a fraction of a cent, for example 15% of EUR 9.99 is EUR 1.4985. Money cannot have a fraction of a cent, so we must fix one rounding rule. In our model we use HALF_EVEN rounding and it is done in only one method, so every part of the program rounds in the same way.

## Question 2

**Answer:**

| Concept | Entity or Value Object | Reason |
|---|---|---|
| Money | Value Object | Two amounts of EUR 10 are the same thing, they can be interchanged. Money has no separate identity, it is known only by its amount and currency. |
| Product | Entity | A product is identified by its SKU. Its name and price can change over time, still it remains the same product. |
| Cart | Entity | A cart has its own cart id. Items are added and removed in it, but the cart remains the same cart. |
| Percentage | Value Object | A percentage is known only by its value. 25% discount is 25% everywhere, it has no identity of its own. |
| Quantity | Value Object | Quantity is known only by its number, like 3 pieces. Two quantities of 3 are equal and it has no identity. |

## Question 3

**Answer:**

```java
public final class Percentage {

    private final int basisPoints;   // 100% = 10000 basis points

    private Percentage(int basisPoints) {
        this.basisPoints = basisPoints;
    }

    public static Percentage ofBasisPoints(int bp) {
        if (bp < 0 || bp > 10000) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        return new Percentage(bp);
    }

    public static Percentage ofPercent(int percent) {
        return ofBasisPoints(percent * 100);
    }

    // returns the price after removing this discount
    public Money applyTo(Money price) {
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
}
```

The full working code of `Percentage` together with `Money` is given in the code files with this answer (in Money, the discount amount is calculated using the basis points and HALF_EVEN rounding).

The constructor is kept private so that nobody can directly write `new Percentage(25000)` and create a wrong object. The object can be created only through the `ofBasisPoints` and `ofPercent` methods, and these methods first check that the value is between 0 and 100. So checking is not left to the caller, it always happens. Because of this, an invalid Percentage object can never be created in the program.

## Question 4

**Answer:**

We are against this idea. Our reasons are:

- **Purity:** `plus` should be a simple addition. If the same two amounts are added, the result should always come the same. But the exchange rate changes every day, so on one day EUR + USD will give one answer and on another day it will give a different answer. Addition will no longer be a pure function.
- **Testability:** If `plus` uses today's rate, then a test written today can fail tomorrow without any change in our code, only because the rate changed. Testing such a method is very difficult because we cannot control the rate and the date inside a normal unit test.
- **Invariants:** One main rule of this model is that money of different currencies cannot be added directly. If `plus` silently converts, this rule is broken and the mistake will not be seen. Also nobody will know which rate was used and how rounding was done.

So conversion should not be hidden inside `plus`. It should be a separate work done by a service, where the exchange rate and the date are given openly. Then the rate used can also be saved on the invoice.

## Question 5

**Answer:**

For refunds we should not change the `Money` class to accept negative values. If we do that, then a negative product price also becomes possible everywhere, and the rule "price is never negative" will be lost.

Instead, we keep `Money` as it is (never negative, used for prices and amounts to be paid) and we add one new type for signed amounts:

| Type | Meaning |
|---|---|
| `Money` (no change) | Only positive or zero amounts. Used for product price, line total and amount to pay. Negative price is still not possible. |
| `SignedMoney` (new) | Amount which can be negative also. Used only for ledger entries, adjustments and refunds. |
| `Refund` (new) | One refund against a payment. It stores the payment id, the refund amount as a normal positive `Money`, and the reason. |

Working: when a refund is given, we make a `Refund` object with a positive amount and post a negative entry of the same amount in `SignedMoney` in the customer's ledger. The balance of the customer is calculated in `SignedMoney`, so a negative balance simply means the shop has to return money to the customer. At the time of payment, we check the signed balance: if it is positive, we convert it to `Money` and take payment; if it is zero or negative, no payment is taken. The conversion from `SignedMoney` to `Money` is done at this one place only and it rejects negative values, so a price can still never become negative.
