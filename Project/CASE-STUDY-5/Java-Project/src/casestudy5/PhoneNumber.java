package casestudy5;

import java.util.regex.Pattern;

/**
 * Value object for a phone number.
 *
 * Invariant: a phone number is stored in ONE canonical format only -
 * international E.164 form, e.g. +919876543210.
 * Spaces, dashes and brackets a user types are stripped during
 * construction, so the "same number stored in different formats"
 * problem cannot occur downstream.
 *
 * Immutable, equality by normalised value -> value object.
 */
public record PhoneNumber(String value) {

    // E.164: '+' then country code (first digit 1-9), 8 to 15 digits total.
    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    public PhoneNumber {
        if (value == null) {
            throw new DomainException("Phone number must not be null");
        }
        value = value.replaceAll("[\\s\\-()]", "");
        if (!E164.matcher(value).matches()) {
            throw new DomainException(
                    "Phone number must be in international format (+<country code><number>): " + value);
        }
    }

    public static PhoneNumber of(String raw) {
        return new PhoneNumber(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
