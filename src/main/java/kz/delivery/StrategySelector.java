package kz.delivery;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Business policy selects an algorithm; algorithms select routes. */
public final class StrategySelector {
    private final Map<DeliveryPriority, RouteStrategy> strategies;

    public StrategySelector(Map<DeliveryPriority, RouteStrategy> strategies) {
        var copy = new EnumMap<DeliveryPriority, RouteStrategy>(DeliveryPriority.class);
        copy.putAll(Objects.requireNonNull(strategies));
        for (DeliveryPriority priority : DeliveryPriority.values()) {
            Objects.requireNonNull(copy.get(priority), "Missing strategy mapping for " + priority);
        }
        this.strategies = Map.copyOf(copy);
    }

    public RouteStrategy select(DeliveryRequest request) {
        Objects.requireNonNull(request, "Request is required");
        // Urgency takes precedence; otherwise an eco preference overrides the regular mapping.
        DeliveryPriority key = request.priority() == DeliveryPriority.URGENT
                ? DeliveryPriority.URGENT
                : request.ecoPreferred() ? DeliveryPriority.ECO : request.priority();
        return strategies.get(key);
    }
}
