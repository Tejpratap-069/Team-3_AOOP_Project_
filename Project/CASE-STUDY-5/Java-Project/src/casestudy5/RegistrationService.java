package casestudy5;

import java.time.Instant;

/**
 * Application/domain service that owns the CROSS-AGGREGATE rule:
 * "every email address is unique across ALL users".
 *
 * A single UserAccount object cannot enforce this (it cannot see the
 * other accounts), exactly like "do not place an order if stock is
 * insufficient" could not live inside Order in Case Study 2.
 *
 * Defence in depth:
 *  1. This service checks the repository BEFORE creating the account.
 *     Because EmailAddress is normalised at construction, the check is
 *     automatically case-insensitive ("Tej@X.com" == "tej@x.com").
 *  2. The check alone has a race: two simultaneous registrations can
 *     both pass it. So the database must ALSO carry a UNIQUE constraint
 *     on the normalised email column; a constraint violation on save is
 *     translated into the same "email already registered" error.
 */
public final class RegistrationService {

    private final UserRepository repository;

    public RegistrationService(UserRepository repository) {
        this.repository = repository;
    }

    public UserAccount register(String rawEmail, Instant now) {
        EmailAddress email = EmailAddress.of(rawEmail); // validates + normalises
        if (repository.existsByEmail(email)) {
            throw new DomainException("Email already registered: " + email);
        }
        UserAccount account = UserAccount.register(email, now);
        repository.save(account);
        return account;
    }
}
