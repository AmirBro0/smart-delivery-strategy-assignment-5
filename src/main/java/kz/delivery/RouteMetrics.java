package kz.delivery;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record RouteMetrics(int effectiveMinutes, double totalCost,
                           double distanceKm, double emissionKg, double prioritySurcharge) {
    public RouteMetrics {
        Checks.nonNegative(effectiveMinutes, "Effective minutes");
        Checks.nonNegative(totalCost, "Total cost");
        Checks.nonNegative(distanceKm, "Distance");
        Checks.nonNegative(emissionKg, "Emissions");
        Checks.nonNegative(prioritySurcharge, "Priority surcharge");
    }

    public static RouteMetrics calculate(Route route, DeliveryRequest request) {
        // Round once, after adding every monetary component, to two decimal places.
        BigDecimal total = BigDecimal.valueOf(route.baseCost())
                .add(BigDecimal.valueOf(route.distanceKm()).multiply(BigDecimal.valueOf(route.costPerKm())))
                .add(BigDecimal.valueOf(request.priority().surcharge()))
                .add(BigDecimal.valueOf(route.additionalFees()));
        return new RouteMetrics(
                route.estimatedMinutes() + route.trafficDelayMinutes() + route.weatherDelayMinutes(),
                total.setScale(2, RoundingMode.HALF_UP).doubleValue(),
                route.distanceKm(), route.carbonEmissionKg(), request.priority().surcharge());
    }
}
