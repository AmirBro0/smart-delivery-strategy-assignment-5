package kz.delivery;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record RouteAssessment(Route route, RouteMetrics metrics,
                              List<String> rejectionReasons, Optional<RouteScore> score) {
    public RouteAssessment {
        Objects.requireNonNull(route);
        Objects.requireNonNull(metrics);
        rejectionReasons = List.copyOf(rejectionReasons);
        Objects.requireNonNull(score);
        if (!rejectionReasons.isEmpty() && score.isPresent()) {
            throw new IllegalArgumentException("Rejected routes must not be scored");
        }
    }

    public boolean eligible() { return rejectionReasons.isEmpty(); }

    public RouteAssessment withScore(RouteScore newScore) {
        return new RouteAssessment(route, metrics, rejectionReasons, Optional.of(newScore));
    }
}
