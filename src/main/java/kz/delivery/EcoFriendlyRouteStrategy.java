package kz.delivery;

import java.util.List;
import java.util.Locale;

public final class EcoFriendlyRouteStrategy extends AbstractRouteStrategy {
    private final double emissionWeight;
    private final double distanceWeight;

    public EcoFriendlyRouteStrategy() { this(1.0, 0.05); }

    public EcoFriendlyRouteStrategy(double emissionWeight, double distanceWeight) {
        Checks.nonNegative(emissionWeight, "Emission weight");
        Checks.nonNegative(distanceWeight, "Distance weight");
        if (emissionWeight == 0 && distanceWeight == 0) {
            throw new IllegalArgumentException("At least one eco weight must be positive");
        }
        this.emissionWeight = emissionWeight;
        this.distanceWeight = distanceWeight;
    }

    @Override public String getStrategyName() { return "Eco Friendly"; }

    @Override
    protected RouteScore score(Route route, RouteMetrics m, List<RouteMetrics> eligible) {
        double value = m.emissionKg() * emissionWeight + m.distanceKm() * distanceWeight;
        return new RouteScore(value, String.format(Locale.ROOT,
                "Eco score = %.2f kg * %.2f + %.2f km * %.2f = %.4f",
                m.emissionKg(), emissionWeight, m.distanceKm(), distanceWeight, value));
    }
}
