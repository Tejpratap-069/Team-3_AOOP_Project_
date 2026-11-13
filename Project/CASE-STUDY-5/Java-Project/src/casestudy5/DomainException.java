package casestudy5;

/** Thrown when a domain rule (invariant) would be violated. */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
