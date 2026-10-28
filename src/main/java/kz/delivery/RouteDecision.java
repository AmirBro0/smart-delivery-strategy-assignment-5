package kz.delivery;

import java.util.List;
import java.util.Objects;

public record RouteDecision(String strategyName, Route selectedRoute, RouteMetrics metrics,
                            double score, String explanation, List<RouteAssessment> assessments) {
    public RouteDecision {
        strategyName = Checks.text(strategyName, "Strategy name");
        Objects.requireNonNull(selectedRoute);
        Objects.requireNonNull(metrics);
        Checks.nonNegative(score, "Score");
        explanation = Checks.text(explanation, "Explanation");
        assessments = List.copyOf(assessments);
    }
}
