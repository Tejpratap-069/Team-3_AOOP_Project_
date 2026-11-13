package casestudy5;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * ENTITY and AGGREGATE ROOT for user accounts.
 *
 * Self-enforcing design (compare the guarded Order of Case Study 2, Model A):
 *  - every field is private; there are NO setters,
 *  - state changes happen only through intention-revealing methods,
 *  - each method checks its guard first and throws DomainException
 *    if the transition or value would break an invariant,
 *  - optional contact details are Optional<complete value object>:
 *    a phone/address is either fully valid or absent - never partial.
 *
 * Note what is NOT here: email uniqueness across users. A single
 * UserAccount cannot see the other accounts, so that rule cannot be
 * its invariant - it is enforced by RegistrationService + the database
 * (see Q5 of the solution document).
 */
public final class UserAccount {

    public static final Duration TOKEN_VALIDITY = Duration.ofHours(24);

    private final UserId id;
    private EmailAddress email;
    private EmailVerification verification;
    private AccountStatus status;
    private Optional<PhoneNumber> phone;
    private Optional<PostalAddress> address;
    private final Instant registeredAt;

    private UserAccount(UserId id, EmailAddress email, VerificationToken firstToken, Instant now) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.verification = new EmailVerification.Unverified(firstToken, now);
        this.status = AccountStatus.PENDING_VERIFICATION;
        this.phone = Optional.empty();
        this.address = Optional.empty();
        this.registeredAt = now;
    }

    /** Registration: a new account is ALWAYS born unverified and pending. */
    public static UserAccount register(EmailAddress email, Instant now) {
        VerificationToken token = VerificationToken.issue(now, TOKEN_VALIDITY);
        return new UserAccount(UserId.newId(), email, token, now);
    }

    // ------------------------------------------------ verification

    /**
     * Verifies the email using the pending token.
     * Guards: account must be pending, token must match, token must not be expired.
     * On success the Unverified value is REPLACED by a Verified value -
     * the token ceases to exist on this account, so it cannot be replayed.
     */
    public void verifyEmail(String candidateToken, Instant now) {
        if (status == AccountStatus.DELETED) {
            throw new DomainException("A deleted account cannot be verified");
        }
        if (!(verification instanceof EmailVerification.Unverified unverified)) {
            throw new DomainException("Email is already verified");
        }
        VerificationToken token = unverified.token();
        if (token.isExpired(now)) {
            throw new DomainException("Verification token has expired - request a new one");
        }
        if (!token.matches(candidateToken)) {
            throw new DomainException("Verification token does not match");
        }
        this.verification = new EmailVerification.Verified(now);
        this.status = AccountStatus.ACTIVE;
    }

    /** Issues a replacement token (e.g. after expiry). The old token is discarded. */
    public VerificationToken resendVerificationToken(Instant now) {
        if (!(verification instanceof EmailVerification.Unverified)) {
            throw new DomainException("Email is already verified");
        }
        VerificationToken fresh = VerificationToken.issue(now, TOKEN_VALIDITY);
        this.verification = new EmailVerification.Unverified(fresh, now);
        return fresh;
    }

    /** The currently pending token, if the account is still unverified. */
    public Optional<VerificationToken> pendingToken() {
        if (verification instanceof EmailVerification.Unverified u) {
            return Optional.of(u.token());
        }
        return Optional.empty();
    }

    // ------------------------------------------------ lifecycle

    /**
     * Changing the email RESETS verification: the new address is unverified,
     * a fresh token is issued, and the account returns to
     * PENDING_VERIFICATION until the new address is verified.
     * (Uniqueness of the new address is checked by the application
     *  service before this method is called - see Q5.)
     */
    public VerificationToken changeEmail(EmailAddress newEmail, Instant now) {
        if (status == AccountStatus.DELETED) {
            throw new DomainException("A deleted account cannot change email");
        }
        this.email = Objects.requireNonNull(newEmail);
        VerificationToken fresh = VerificationToken.issue(now, TOKEN_VALIDITY);
        this.verification = new EmailVerification.Unverified(fresh, now);
        this.status = AccountStatus.PENDING_VERIFICATION;
        return fresh;
    }

    public void suspend() {
        requireStatus(AccountStatus.ACTIVE, "suspend");
        this.status = AccountStatus.SUSPENDED;
    }

    public void reactivate() {
        requireStatus(AccountStatus.SUSPENDED, "reactivate");
        this.status = AccountStatus.ACTIVE;
    }

    public void delete() {
        if (status == AccountStatus.DELETED) {
            throw new DomainException("Account is already deleted");
        }
        this.status = AccountStatus.DELETED;
    }

    /**
     * Login guard: ONLY an ACTIVE account may log in.
     * Suspended, pending-verification and deleted accounts are all
     * rejected by this single rule - the "suspended users can still
     * log in" bug is prevented here, at the domain level.
     */
    public void requireCanLogin() {
        if (status != AccountStatus.ACTIVE) {
            throw new DomainException("Login not allowed for account in state " + status);
        }
    }

    public boolean canLogin() {
        return status == AccountStatus.ACTIVE;
    }

    // ------------------------------------------------ contact details

    /** Optional contact info: validated as a whole, or not stored at all. */
    public void setPhone(PhoneNumber phone) {
        this.phone = Optional.of(Objects.requireNonNull(phone));
    }

    public void clearPhone() {
        this.phone = Optional.empty();
    }

    public void setAddress(PostalAddress address) {
        this.address = Optional.of(Objects.requireNonNull(address));
    }

    public void clearAddress() {
        this.address = Optional.empty();
    }

    // ------------------------------------------------ accessors (no setters)

    public UserId id() { return id; }
    public EmailAddress email() { return email; }
    public EmailVerification verification() { return verification; }
    public AccountStatus status() { return status; }
    public Optional<PhoneNumber> phone() { return phone; }
    public Optional<PostalAddress> address() { return address; }
    public Instant registeredAt() { return registeredAt; }
    public boolean emailVerified() { return verification.isVerified(); }

    private void requireStatus(AccountStatus expected, String operation) {
        if (status != expected) {
            throw new DomainException(
                    "Cannot " + operation + " an account in state " + status + " (requires " + expected + ")");
        }
    }
}
