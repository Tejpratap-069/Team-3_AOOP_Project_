package casestudy5;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

/**
 * Value object bundling a verification token code TOGETHER WITH its expiry.
 *
 * In the naive model the token and its expiry were two separate nullable
 * fields, so they could disagree (a token with no expiry, an expiry with
 * no token). Here they are one indivisible value: a token always has
 * exactly one expiry, fixed at issue time.
 *
 * Immutable, defined entirely by (code, expiresAt), replaced (not mutated)
 * when a new token is issued -> value object.
 */
public record VerificationToken(String code, Instant expiresAt) {

    private static final SecureRandom RANDOM = new SecureRandom();

    public VerificationToken {
        Objects.requireNonNull(code, "token code must not be null");
        Objects.requireNonNull(expiresAt, "token expiry must not be null");
        if (code.isBlank()) {
            throw new DomainException("Verification token code must not be blank");
        }
    }

    /** Issues a fresh cryptographically random token valid for the given duration. */
    public static VerificationToken issue(Instant now, Duration validity) {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new VerificationToken(code, now.plus(validity));
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public boolean matches(String candidate) {
        return code.equals(candidate);
    }
}
