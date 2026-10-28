package kz.delivery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static kz.delivery.TestData.*;

class ConstraintsTest {
    @Test void everyStrategyRejectsUnavailableRouteWithBestMetrics() {
        Route closed = new Route("closed", DemoData.ORIGIN, DemoData.DESTINATION, 0, 0,
                0, 0, 0, 0, 0, 0, false, Set.of());
        for (RouteStrategy strategy : DemoData.strategies()) {
            assertEquals("valid", strategy.selectRoute(List.of(closed, route("valid", 10, 100, 5, 1)), request()).selectedRoute().id());
        }
    }
    @Test void budgetRestrictionUsesFullCostIncludingSurcharge() {
        DeliveryRequest standard = DemoData.request("budget", DeliveryPriority.STANDARD, 200, 100, 100, false);
        assertThrows(NoSuitableRouteException.class, () -> new CheapestRouteStrategy()
                .selectRoute(List.of(route("A", 10, 100, 5, 1)), standard));
    }
    @Test void deadlineRestrictionUsesTrafficAndWeather() {
        Route r = new Route("A", DemoData.ORIGIN, DemoData.DESTINATION, 5, 10,
                100, 0, 0, 1, 5, 6, true, Set.of());
        var ex = assertThrows(NoSuitableRouteException.class, () -> new FastestRouteStrategy()
                .selectRoute(List.of(r), request(1000, 20, 10)));
        assertTrue(ex.assessments().get(0).rejectionReasons().contains("Deadline exceeded"));
    }
    @Test void emissionLimitIsHardConstraintEvenForFastest() {
        assertEquals("B", new FastestRouteStrategy().selectRoute(List.of(
                route("A", 1, 100, 5, 10), route("B", 20, 100, 5, 1)), request(1000, 100, 2)).selectedRoute().id());
    }
    @Test void allStrategiesApplyBudgetDeadlineAndEmissionConstraints() {
        var routes = List.of(route("expensive", 1, 1001, 1, 0),
                route("late", 101, 1, 1, 0), route("dirty", 1, 1, 1, 3), route("valid", 20, 100, 5, 1));
        for (RouteStrategy strategy : DemoData.strategies()) {
            assertEquals("valid", strategy.selectRoute(routes, request(1000, 100, 2)).selectedRoute().id());
        }
    }
    @Test void exactBudgetDeadlineAndEmissionLimitsAreInclusive() {
        RouteDecision d = new FastestRouteStrategy().selectRoute(List.of(route("A", 20, 100, 5, 2)), request(100, 20, 2));
        assertEquals("A", d.selectedRoute().id());
    }
    @Test void prohibitedPriorityIsRejected() {
        Route r = new Route("A", DemoData.ORIGIN, DemoData.DESTINATION, 5, 10,
                100, 0, 0, 1, 0, 0, true, Set.of(DeliveryPriority.URGENT));
        var ex = assertThrows(NoSuitableRouteException.class, () -> new FastestRouteStrategy().selectRoute(List.of(r),
                DemoData.request("urgent", DeliveryPriority.URGENT, 10000, 100, 20, false)));
        assertTrue(ex.assessments().get(0).rejectionReasons().contains("Priority prohibited on route"));
    }
    @Test void requestSpecificBlockedRouteIsRejected() {
        DeliveryRequest blocked = new DeliveryRequest("blocked", DemoData.ORIGIN, DemoData.DESTINATION,
                DeliveryPriority.ECONOMY, 10000, 100, 20, false, Set.of("A"));
        assertEquals("B", new FastestRouteStrategy().selectRoute(List.of(
                route("A", 1, 100, 5, 1), route("B", 20, 100, 5, 1)), blocked).selectedRoute().id());
    }
    @Test void endpointMismatchIsRejected() {
        Route wrong = new Route("A", new Location("Other", 0, 0), DemoData.DESTINATION,
                1, 1, 1, 0, 0, 0, 0, 0, true, Set.of());
        var ex = assertThrows(NoSuitableRouteException.class, () -> new FastestRouteStrategy().selectRoute(List.of(wrong), request()));
        assertTrue(ex.assessments().get(0).rejectionReasons().get(0).contains("Origin or destination"));
    }
    @Test void noSuitableRouteRetainsAllRejectionReasons() {
        var ex = assertThrows(NoSuitableRouteException.class, () -> new FastestRouteStrategy()
                .selectRoute(List.of(route("A", 30, 200, 10, 5)), request(100, 20, 1)));
        assertEquals(3, ex.assessments().get(0).rejectionReasons().size());
        assertTrue(ex.getMessage().contains("all candidates violate constraints"));
    }
    @Test void emptyRoutesThrowExplicitException() {
        var ex = assertThrows(NoSuitableRouteException.class, () -> new FastestRouteStrategy().selectRoute(List.of(), request()));
        assertTrue(ex.getMessage().contains("candidate list is empty"));
    }
    @Test void negativeDistanceCostAndBudgetAreRejectedAtConstruction() {
        assertThrows(IllegalArgumentException.class, () -> route("A", 10, 100, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> route("A", 10, -1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> request(-1, 100, 10));
    }
    @Test void nullRequestAndNullCandidateAreRejectedExplicitly() {
        assertThrows(NullPointerException.class, () -> new FastestRouteStrategy().selectRoute(DemoData.routes(), null));
        var routes = new ArrayList<Route>(); routes.add(null);
        assertThrows(NullPointerException.class, () -> new FastestRouteStrategy().selectRoute(routes, request()));
    }
    @Test void duplicateRouteIdsAreRejected() {
        Route a = route("A", 20, 100, 5, 1);
        assertThrows(IllegalArgumentException.class, () -> new FastestRouteStrategy().selectRoute(List.of(a, a), request()));
    }
    @Test void nonFiniteDomainValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> request(Double.NaN, 100, 10));
        assertThrows(IllegalArgumentException.class, () -> route("A", 20, 100, Double.POSITIVE_INFINITY, 1));
        assertThrows(IllegalArgumentException.class, () -> new Location("A", 91, 0));
    }
    @Test void timeOverflowIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Route("A", DemoData.ORIGIN, DemoData.DESTINATION,
                1, Integer.MAX_VALUE, 1, 0, 0, 1, 1, 0, true, Set.of()));
    }
    @Test void decisionsAndRejectionListsAreImmutable() {
        RouteDecision d = new FastestRouteStrategy().selectRoute(DemoData.routes(), DemoData.standardRequest());
        assertThrows(UnsupportedOperationException.class, () -> d.assessments().clear());
        assertThrows(UnsupportedOperationException.class, () -> d.assessments().get(7).rejectionReasons().clear());
    }
}
