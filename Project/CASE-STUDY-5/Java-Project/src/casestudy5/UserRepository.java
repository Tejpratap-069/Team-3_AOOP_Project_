package casestudy5;

import java.util.Optional;

/**
 * Repository abstraction for accounts. In production this is backed by
 * a database table with a UNIQUE constraint on the normalised email
 * column - the final line of defence for the uniqueness rule.
 */
public interface UserRepository {
    boolean existsByEmail(EmailAddress email);
    void save(UserAccount account);
    Optional<UserAccount> findByEmail(EmailAddress email);
}
