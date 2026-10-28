package kz.delivery;

import java.util.Objects;
import java.util.Set;

public record Route(
        String id, Location origin, Location destination,
        double distanceKm, int estimatedMinutes,
        double baseCost, double costPerKm, double additionalFees,
        double carbonEmissionKg, int trafficDelayMinutes, int weatherDelayMinutes,
        boolean available, Set<DeliveryPriority> prohibitedPriorities) {

    public Route {
        id = Checks.text(id, "Route id");
        Objects.requireNonNull(origin, "Origin is required");
        Objects.requireNonNull(destination, "Destination is required");
        Checks.nonNegative(distanceKm, "Distance");
        Checks.nonNegative(estimatedMinutes, "Estimated minutes");
        Checks.nonNegative(baseCost, "Base cost");
        Checks.nonNegative(costPerKm, "Cost per km");
        Checks.nonNegative(additionalFees, "Additional fees");
        Checks.nonNegative(carbonEmissionKg, "Carbon emissions");
        Checks.nonNegative(trafficDelayMinutes, "Traffic delay");
        Checks.nonNegative(weatherDelayMinutes, "Weather delay");
        if ((long) estimatedMinutes + trafficDelayMinutes + weatherDelayMinutes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Effective delivery time overflows int");
        }
        if (!Double.isFinite(baseCost + distanceKm * costPerKm + additionalFees + 500)) {
            throw new IllegalArgumentException("Total delivery cost overflows double");
        }
        prohibitedPriorities = Set.copyOf(Objects.requireNonNull(prohibitedPriorities));
    }
}
