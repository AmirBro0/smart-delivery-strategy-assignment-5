package kz.delivery;

import java.util.List;
import java.util.Locale;

public final class CheapestRouteStrategy extends AbstractRouteStrategy {
    @Override public String getStrategyName() { return "Cheapest"; }

    @Override
    protected RouteScore score(Route route, RouteMetrics m, List<RouteMetrics> eligible) {
        // The surcharge is recoverable from the already rounded request-specific total.
        double surcharge = m.totalCost() - route.baseCost()
                - route.distanceKm() * route.costPerKm() - route.additionalFees();
        return new RouteScore(m.totalCost(), String.format(Locale.ROOT,
                "Cost = %.2f base + %.2f distance + %.2f priority + %.2f fees = %.2f KZT",
                route.baseCost(), route.distanceKm() * route.costPerKm(), surcharge,
                route.additionalFees(), m.totalCost()));
    }
}
