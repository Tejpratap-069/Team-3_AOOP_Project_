package casestudy2;

// Exception thrown when a business rule of the order model is broken.
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
