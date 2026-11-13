package casestudy5;

/**
 * Lifecycle of a user account, as a closed set (an enum) instead of a
 * free-text String or a pile of booleans.
 *
 * Permitted transitions (enforced by UserAccount's guarded methods):
 *   PENDING_VERIFICATION --verifyEmail--> ACTIVE
 *   ACTIVE               --suspend-----> SUSPENDED
 *   SUSPENDED            --reactivate--> ACTIVE
 *   ACTIVE / SUSPENDED / PENDING_VERIFICATION --changeEmail--> PENDING_VERIFICATION
 *   any non-deleted state --delete-----> DELETED   (terminal)
 */
public enum AccountStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    SUSPENDED,
    DELETED
}
