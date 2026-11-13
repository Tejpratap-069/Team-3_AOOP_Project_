package casestudy5;

import java.time.Instant;
import java.util.Objects;

/**
 * The email-verification state of an account, modelled as a TYPE PER STATE
 * (the same idea as Model B in Case Study 2).
 *
 * This replaces the naive triple (boolean emailVerified, Instant verifiedAt,
 * String verificationToken + tokenExpiry) which allowed contradictory
 * combinations such as:
 *   - verified == true  but verifiedAt == null   ("marked verified, no date recorded")
 *   - verified == false but verifiedAt != null
 *   - a live token still sitting on an already-verified account
 *
 * With a sealed type those states are UNREPRESENTABLE:
 *   - an account is either Unverified (it holds a token, and NO verifiedAt
 *     field exists on that type at all), or
 *   - Verified (verifiedAt is a mandatory constructor argument, and NO
 *     token field exists on that type at all).
 */
public sealed interface EmailVerification
        permits EmailVerification.Unverified, EmailVerification.Verified {

    /** Not yet verified: the only data that can exist is the pending token. */
    record Unverified(VerificationToken token, Instant issuedAt) implements EmailVerification {
        public Unverified {
            Objects.requireNonNull(token);
            Objects.requireNonNull(issuedAt);
        }
    }

    /** Verified: verifiedAt MUST be supplied - "verified with no date" cannot be built. */
    record Verified(Instant verifiedAt) implements EmailVerification {
        public Verified {
            Objects.requireNonNull(verifiedAt, "A verified account must record when it was verified");
        }
    }

    default boolean isVerified() {
        return this instanceof Verified;
    }
}
