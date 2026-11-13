# Case Study 1 — Java Project (Money & Pricing)

Refined model for Case Study 1: `Money` stored as long minor units with a `Currency`, `Percentage` in basis points (0–10000), `Quantity` of at least 1, plus `Product` and `Cart`. Wrong operations (negative money, extra decimal places for the currency, mixing currencies, discount above 100%, quantity 0) are rejected by the types themselves.

## How to run

From this `Java-Project` folder:

```
javac -d out src\casestudy1\*.java
java -cp out casestudy1.Main
```

## Output

The demo prints the cart total (EUR 528.97), discount results, JPY/KWD prices, and then shows each wrong operation being rejected. The full output is saved in `output.txt`.
