package kz.delivery;

public enum DeliveryPriority {
    URGENT(500), ECONOMY(0), ECO(0), STANDARD(150);

    private final double surcharge;

    DeliveryPriority(double surcharge) { this.surcharge = surcharge; }

    public double surcharge() { return surcharge; }
}
