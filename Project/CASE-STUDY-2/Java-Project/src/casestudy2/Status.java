package casestudy2;

// The states of an order. Using an enum instead of a free String,
// so a spelling like "SHIPED" can never be saved as a state.
public enum Status {
    DRAFT, PLACED, PAID, SHIPPED, DELIVERED, CANCELLED
}
