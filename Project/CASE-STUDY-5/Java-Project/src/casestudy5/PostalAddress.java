package casestudy5;

/**
 * Value object for a postal address.
 *
 * Invariant: an address is COMPLETE or it does not exist.
 * All four components are required and non-blank, so the "incomplete
 * address" problem (a street with no city, a missing postal code...)
 * is unrepresentable - there is no way to build a half-filled address.
 * A user either has a complete PostalAddress or has none at all
 * (Optional<PostalAddress> on the account).
 *
 * Immutable, equality by all components -> value object.
 */
public record PostalAddress(String street, String city, String postalCode, String country) {

    public PostalAddress {
        street = requireNonBlank(street, "street");
        city = requireNonBlank(city, "city");
        postalCode = requireNonBlank(postalCode, "postalCode");
        country = requireNonBlank(country, "country");
    }

    private static String requireNonBlank(String field, String name) {
        if (field == null || field.isBlank()) {
            throw new DomainException("Postal address field '" + name + "' must not be blank");
        }
        return field.trim();
    }
}
