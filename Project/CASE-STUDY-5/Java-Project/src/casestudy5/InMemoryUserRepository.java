package casestudy5;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Simple in-memory implementation used by the demo and tests. */
public final class InMemoryUserRepository implements UserRepository {

    private final Map<String, UserAccount> byNormalisedEmail = new HashMap<>();

    @Override
    public synchronized boolean existsByEmail(EmailAddress email) {
        return byNormalisedEmail.containsKey(email.value());
    }

    @Override
    public synchronized void save(UserAccount account) {
        byNormalisedEmail.put(account.email().value(), account);
    }

    @Override
    public synchronized Optional<UserAccount> findByEmail(EmailAddress email) {
        return Optional.ofNullable(byNormalisedEmail.get(email.value()));
    }
}
