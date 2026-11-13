# Case Study 2 — Java Project (Order Lifecycle)

Model A for Case Study 2: one `Order` class with a private `Status` and guarded methods — `place()`, `pay()`, `ship()`, `deliver()`, `cancel()`. The total is always calculated from the lines, shipment details can only be created inside `ship()`, and the customer's credit-limit rule is kept in `OrderPlacementService`, which checks and reserves credit together.

## How to run

From this `Java-Project` folder:

```
javac -d out src\casestudy2\*.java
java -cp out casestudy2.Main
```

## Output

The demo runs the full lifecycle DRAFT → PLACED → PAID → SHIPPED → DELIVERED and then shows the wrong operations being rejected. It ends with `ALL DEMO CHECKS PASSED (13 checks)`. The full output is saved in `output.txt`.
