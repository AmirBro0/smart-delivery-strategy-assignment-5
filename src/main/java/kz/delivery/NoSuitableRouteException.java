package kz.delivery;

import java.util.List;

public final class NoSuitableRouteException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final List<RouteAssessment> assessments;

    public NoSuitableRouteException(String message, List<RouteAssessment> assessments) {
        super(message);
        this.assessments = List.copyOf(assessments);
    }

    public List<RouteAssessment> assessments() { return assessments; }
}
