# Case Study 5 — Java Project (User Accounts)

Domain model for Case Study 5: `EmailAddress` (validated and normalised), `PhoneNumber` (E.164), `PostalAddress` (all parts compulsory), `VerificationToken` (code + expiry together), and email verification as a sealed type — `Unverified` holds a token, `Verified` holds the verification date, so a "verified" account without a date cannot exist. `UserAccount` guards its own lifecycle, and email uniqueness across accounts is enforced by `RegistrationService`.

## How to run

From this `Java-Project` folder:

```
javac -d out src\casestudy5\*.java
java -cp out casestudy5.Main
```

## Output

The demo registers and verifies a user, then shows the wrong operations being rejected (bad email formats, duplicate email with different capitalisation, expired or replayed tokens, login before verification, suspended/deleted login). It ends with `ALL DEMO CHECKS PASSED (18 checks)`. The full output is saved in `output.txt`.
