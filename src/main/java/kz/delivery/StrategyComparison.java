package kz.delivery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class StrategyComparison {
    private final List<RouteStrategy> strategies;

    public StrategyComparison(List<RouteStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
        if (this.strategies.isEmpty()) throw new IllegalArgumentException("Comparison needs at least one strategy");
    }

    public List<ComparisonResult> compare(List<Route> routes, DeliveryRequest request) {
        var result = new ArrayList<ComparisonResult>();
        for (RouteStrategy strategy : strategies) {
            try {
                RouteDecision decision = new RoutePlanner(strategy).plan(routes, request);
                result.add(new ComparisonResult(strategy.getStrategyName(), Optional.of(decision), Optional.empty()));
            } catch (NoSuitableRouteException exception) {
                result.add(new ComparisonResult(strategy.getStrategyName(), Optional.empty(), Optional.of(exception.getMessage())));
            }
        }
        return List.copyOf(result);
    }
}
