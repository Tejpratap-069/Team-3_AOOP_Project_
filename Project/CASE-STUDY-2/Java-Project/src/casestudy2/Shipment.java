package casestudy2;

import java.time.Instant;

// Shipment details of an order. A Shipment object is created only at the
// time of shipping, so tracking number and shipped time always come
// together - an order cannot have a tracking number without being shipped.
public record Shipment(String trackingNumber, Instant shippedAt) {
}
