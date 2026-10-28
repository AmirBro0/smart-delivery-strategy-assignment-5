package kz.delivery;

import java.util.List;
import java.util.Locale;

public final class FastestRouteStrategy extends AbstractRouteStrategy {
    @Override public String getStrategyName() { return "Fastest"; }

    @Override
    protected RouteScore score(Route route, RouteMetrics m, List<RouteMetrics> eligible) {
        return new RouteScore(m.effectiveMinutes(), String.format(Locale.ROOT,
                "Time = %d base + %d traffic + %d weather = %d min",
                route.estimatedMinutes(), route.trafficDelayMinutes(), route.weatherDelayMinutes(), m.effectiveMinutes()));
    }
}
