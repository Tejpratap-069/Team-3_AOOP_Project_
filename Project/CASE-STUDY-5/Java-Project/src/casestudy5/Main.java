package casestudy5;

import java.time.Duration;
import java.time.Instant;

/**
 * Demo driver: walks through the Case Study 5 scenarios and shows that
 * every bug from the problem statement is now rejected by the model.
 * Expected output ends with: ALL DEMO CHECKS PASSED
 */
public final class Main {

    private static int checks = 0;

    public static void main(String[] args) {
        Instant now = Instant.now();
        RegistrationService service = new RegistrationService(new InMemoryUserRepository());

        // 1. Invalid email formats are rejected at construction.
        expectReject("invalid email format", () -> EmailAddress.of("not-an-email"));
        expectReject("invalid email format (no domain)", () -> EmailAddress.of("tej@"));

        // 2. Normalisation: capitalisation variants are the same address,
        //    so the duplicate-account bug cannot happen.
        UserAccount tej = service.register("Tej@Example.COM", now);
        System.out.println("Registered, normalised email: " + tej.email());
        expectReject("duplicate email with different capitalisation",
                () -> service.register("tej@example.com", now));

        // 3. An unverified (pending) account cannot log in.
        expectReject("login before verification", tej::requireCanLogin);

        // 4. Wrong and expired tokens are rejected; the right one works.
        String token = tej.pendingToken().orElseThrow().code();
        expectReject("wrong verification token", () -> tej.verifyEmail("wrong-token", now));
        expectReject("expired verification token",
                () -> tej.verifyEmail(token, now.plus(Duration.ofHours(25))));

        tej.verifyEmail(token, now.plus(Duration.ofHours(1)));
        System.out.println("After verification: status=" + tej.status()
                + ", verified=" + tej.emailVerified());
        tej.requireCanLogin(); // must NOT throw now
        ok("login allowed after verification");

        // 5. The token no longer exists on the account - it cannot be replayed.
        expectReject("replaying the same token", () -> tej.verifyEmail(token, now));

        // 6. Phone numbers are normalised to one format; bad ones are rejected.
        PhoneNumber phone = PhoneNumber.of("+91 98765-43210");
        System.out.println("Normalised phone: " + phone);
        tej.setPhone(phone);
        expectReject("phone without country code", () -> PhoneNumber.of("9876543210"));

        // 7. Incomplete postal addresses cannot be built.
        expectReject("address with blank city",
                () -> new PostalAddress("MG Road", " ", "500001", "India"));
        tej.setAddress(new PostalAddress("MG Road", "Hyderabad", "500001", "India"));
        ok("complete postal address accepted");

        // 8. Suspended users cannot log in; reactivation restores login.
        tej.suspend();
        expectReject("login while suspended", tej::requireCanLogin);
        tej.reactivate();
        tej.requireCanLogin();
        ok("login restored after reactivation");

        // 9. Illegal lifecycle jumps are rejected.
        expectReject("double suspend",
                () -> { tej.suspend(); tej.suspend(); });
        tej.reactivate();

        // 10. Changing email resets verification and blocks login until re-verified.
        VerificationToken fresh = tej.changeEmail(EmailAddress.of("tej.new@example.com"), now);
        System.out.println("After email change: status=" + tej.status()
                + ", verified=" + tej.emailVerified());
        expectReject("login after email change, before re-verification", tej::requireCanLogin);
        tej.verifyEmail(fresh.code(), now.plus(Duration.ofMinutes(5)));
        tej.requireCanLogin();
        ok("re-verified new email, login allowed again");

        // 11. Deleted accounts are terminal.
        tej.delete();
        expectReject("login after deletion", tej::requireCanLogin);
        expectReject("verifying a deleted account", () -> tej.verifyEmail("anything", now));

        System.out.println("ALL DEMO CHECKS PASSED (" + checks + " checks)");
    }

    private static void expectReject(String what, Runnable action) {
        try {
            action.run();
        } catch (DomainException e) {
            ok("rejected " + what + " -> " + e.getMessage());
            return;
        }
        throw new AssertionError("Expected rejection but it succeeded: " + what);
    }

    private static void ok(String message) {
        checks++;
        System.out.println("  [OK] " + message);
    }
}
