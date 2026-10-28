package kz.delivery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ConstraintChecker {
    public List<RouteAssessment> assess(List<Route> routes, DeliveryRequest request) {
        Objects.requireNonNull(request, "Delivery request is required");
        List<Route> snapshot = List.copyOf(Objects.requireNonNull(routes, "Routes are required"));
        var ids = new HashSet<String>();
        var result = new ArrayList<RouteAssessment>();
        for (Route route : snapshot) {
            if (!ids.add(route.id())) throw new IllegalArgumentException("Duplicate route id: " + route.id());
            RouteMetrics m = RouteMetrics.calculate(route, request);
            var reasons = new ArrayList<String>();
            if (!route.available()) reasons.add("Route unavailable");
            if (!route.origin().equals(request.origin()) || !route.destination().equals(request.destination())) {
                reasons.add("Origin or destination does not match request");
            }
            if (m.totalCost() > request.maxBudget()) reasons.add("Budget exceeded");
            if (m.effectiveMinutes() > request.maxDeliveryMinutes()) reasons.add("Deadline exceeded");
            if (m.emissionKg() > request.maxEmissionKg()) reasons.add("Emission limit exceeded");
            if (request.blockedRouteIds().contains(route.id())) reasons.add("Route blocked for this delivery");
            if (route.prohibitedPriorities().contains(request.priority())) reasons.add("Priority prohibited on route");
            result.add(new RouteAssessment(route, m, reasons, Optional.empty()));
        }
        return List.copyOf(result);
    }
}
