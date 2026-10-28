package kz.delivery;

import java.util.List;
import java.util.Objects;

/** Context: delegates selection to an interchangeable Strategy. */
public final class RoutePlanner {
    private RouteStrategy strategy;

    public RoutePlanner(RouteStrategy strategy) { setStrategy(strategy); }

    public void setStrategy(RouteStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "Strategy is required");
    }

    public RouteDecision plan(List<Route> routes, DeliveryRequest request) {
        return strategy.selectRoute(routes, request);
    }
}
