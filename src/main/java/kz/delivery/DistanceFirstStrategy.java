package kz.delivery;

import java.util.List;
import java.util.Locale;

/** Fifth strategy added after the original four-strategy application passed its tests. */
public final class DistanceFirstStrategy extends AbstractRouteStrategy {
    @Override public String getStrategyName() { return "Distance First"; }

    @Override
    protected RouteScore score(Route route, RouteMetrics metrics, List<RouteMetrics> eligible) {
        return new RouteScore(metrics.distanceKm(), String.format(Locale.ROOT,
                "Distance = %.2f km; choose shortest feasible route", metrics.distanceKm()));
    }
}
