package kz.delivery;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

public final class BalancedRouteStrategy extends AbstractRouteStrategy {
    private final OptimizationWeights weights;

    public BalancedRouteStrategy(double time, double cost, double distance, double emission) {
        this(new OptimizationWeights(time, cost, distance, emission));
    }

    public BalancedRouteStrategy(OptimizationWeights weights) {
        this.weights = Objects.requireNonNull(weights);
    }

    @Override public String getStrategyName() { return "Balanced"; }

    @Override
    protected RouteScore score(Route route, RouteMetrics m, List<RouteMetrics> eligible) {
        double t = normalize(m.effectiveMinutes(), eligible, RouteMetrics::effectiveMinutes);
        double c = normalize(m.totalCost(), eligible, RouteMetrics::totalCost);
        double d = normalize(m.distanceKm(), eligible, RouteMetrics::distanceKm);
        double e = normalize(m.emissionKg(), eligible, RouteMetrics::emissionKg);
        double value = t * weights.time() + c * weights.cost()
                + d * weights.distance() + e * weights.emission();
        return new RouteScore(value, String.format(Locale.ROOT,
                "Balanced score = %.4f*%.2f time + %.4f*%.2f cost + %.4f*%.2f distance + %.4f*%.2f emission = %.4f",
                t, weights.time(), c, weights.cost(), d, weights.distance(), e, weights.emission(), value));
    }

    private static double normalize(double value, List<RouteMetrics> eligible,
                                    ToDoubleFunction<RouteMetrics> field) {
        double min = eligible.stream().mapToDouble(field).min().orElseThrow();
        double max = eligible.stream().mapToDouble(field).max().orElseThrow();
        return max == min ? 0.0 : (value - min) / (max - min);
    }
}
