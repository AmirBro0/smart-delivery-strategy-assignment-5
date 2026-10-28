package kz.delivery;

final class Checks {
    private Checks() {}

    static String text(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    static double nonNegative(double value, String field) {
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException(field + " must be finite and non-negative");
        }
        return value;
    }

    static int nonNegative(int value, String field) {
        if (value < 0) throw new IllegalArgumentException(field + " must be non-negative");
        return value;
    }
}
