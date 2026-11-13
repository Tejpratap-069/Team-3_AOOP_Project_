package casestudy5;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object wrapping the user's identity.
 * Wrapping the raw UUID means a UserId can never be accidentally swapped
 * with any other UUID/String id in method calls (same idea as OrderId in
 * Case Study 2).
 */
public record UserId(UUID value) {
    public UserId {
        Objects.requireNonNull(value, "UserId value must not be null");
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(String raw) {
        try {
            return new UserId(UUID.fromString(raw.trim()));
        } catch (Exception e) {
            throw new DomainException("Invalid user id: " + raw);
        }
    }
}
