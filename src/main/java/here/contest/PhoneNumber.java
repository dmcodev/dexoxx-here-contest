package here.contest;

import java.util.Objects;
import java.util.regex.Pattern;

public record PhoneNumber(String value) {

    public static final int LENGTH = 9;
    private static final Pattern VALIDATION_PATTERN = Pattern.compile("[0-9]{" + LENGTH + "}");

    public PhoneNumber {
        if (Objects.isNull(value) || !VALIDATION_PATTERN.matcher(value).find()) {
            throw new IllegalArgumentException("Invalid phone number value: " + value);
        }
    }

    public int[] digits() {
        return value.chars()
            .map(Character::getNumericValue)
            .toArray();
    }
}
