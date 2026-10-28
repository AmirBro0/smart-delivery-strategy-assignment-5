package kz.delivery;

import java.util.Objects;
import java.util.Set;

public record DeliveryRequest(
        String id, Location origin, Location destination, DeliveryPriority priority,
        double maxBudget, int maxDeliveryMinutes, double maxEmissionKg,
        boolean ecoPreferred, Set<String> blockedRouteIds) {

    public DeliveryRequest {
        id = Checks.text(id, "Request id");
        Objects.requireNonNull(origin, "Origin is required");
        Objects.requireNonNull(destination, "Destination is required");
        Objects.requireNonNull(priority, "Priority is required");
        Checks.nonNegative(maxBudget, "Budget");
        Checks.nonNegative(maxDeliveryMinutes, "Deadline");
        Checks.nonNegative(maxEmissionKg, "Emission limit");
        blockedRouteIds = Set.copyOf(Objects.requireNonNull(blockedRouteIds));
        blockedRouteIds.forEach(idValue -> Checks.text(idValue, "Blocked route id"));
    }
}
