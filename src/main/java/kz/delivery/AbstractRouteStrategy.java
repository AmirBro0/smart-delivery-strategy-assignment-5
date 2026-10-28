package kz.delivery;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Shared feasibility and deterministic ranking, independent of the scoring algorithm. */
public abstract class AbstractRouteStrategy implements RouteStrategy {
    private final ConstraintChecker constraints = new ConstraintChecker();

    @Override
    public final RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        List<RouteAssessment> assessments = constraints.assess(routes, request);
        List<RouteMetrics> eligibleMetrics = assessments.stream()
                .filter(RouteAssessment::eligible).map(RouteAssessment::metrics).toList();
        if (eligibleMetrics.isEmpty()) {
            throw new NoSuitableRouteException("No suitable route for request " + request.id()
                    + (assessments.isEmpty() ? ": candidate list is empty" : ": all candidates violate constraints"),
                    assessments);
        }
        var scored = new ArrayList<RouteAssessment>();
        for (RouteAssessment assessment : assessments) {
            scored.add(assessment.eligible()
                    ? assessment.withScore(score(assessment.route(), assessment.metrics(), eligibleMetrics))
                    : assessment);
        }
        RouteAssessment winner = scored.stream().filter(RouteAssessment::eligible)
                .min(Comparator.comparingDouble((RouteAssessment a) -> a.score().orElseThrow().value())
                        .thenComparing(a -> a.route().id()))
                .orElseThrow();
        RouteScore winningScore = winner.score().orElseThrow();
        return new RouteDecision(getStrategyName(), winner.route(), winner.metrics(),
                winningScore.value(), winningScore.explanation() + "; lowest score; ties use route id", scored);
    }

    protected abstract RouteScore score(Route route, RouteMetrics metrics, List<RouteMetrics> eligibleMetrics);
}
