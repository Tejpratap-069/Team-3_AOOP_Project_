package casestudy5;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Value object for an email address.
 *
 * Invariants enforced at construction (an invalid EmailAddress can never exist):
 *  - format must match a strict email pattern,
 *  - the value is NORMALISED (trimmed + lower-cased) exactly once, here,
 *    so "Tej@Example.com" and "tej@example.com" are the SAME value.
 *    That single normalisation point is what makes the duplicate-account
 *    bug in the problem statement (same email, different capitalisation)
 *    impossible at the domain level.
 *
 * Immutable, equality by value -> value object.
 */
public record EmailAddress(String value) {

    private static final Pattern FORMAT =
            Pattern.compile("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$");

    public EmailAddress {
        if (value == null) {
            throw new DomainException("Email address must not be null");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (!FORMAT.matcher(value).matches()) {
            throw new DomainException("Invalid email address format: " + value);
        }
    }

    public static EmailAddress of(String raw) {
        return new EmailAddress(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
