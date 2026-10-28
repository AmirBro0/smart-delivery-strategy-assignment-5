package kz.delivery;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static kz.delivery.TestData.*;

class DistanceFirstStrategyTest {
    @Test void fifthStrategyChoosesShortestAvailableDemoRoute() {
        assertEquals("R07", selected(new DistanceFirstStrategy()));
    }
    @Test void fifthStrategyRejectsShorterRouteOverBudget() {
        RouteDecision d = new DistanceFirstStrategy().selectRoute(List.of(
                route("short", 20, 500, 1, 1), route("valid", 20, 100, 5, 1)), request(200, 100, 10));
        assertEquals("valid", d.selectedRoute().id());
        assertEquals(5, d.score());
    }
    @Test void fifthStrategyIntegratesWithoutChangingContext() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        planner.setStrategy(new DistanceFirstStrategy());
        assertEquals("R07", planner.plan(DemoData.routes(), DemoData.standardRequest()).selectedRoute().id());
    }
    @Test void fifthStrategyUsesIdForEqualDistanceAndExplainsResult() {
        var d = new DistanceFirstStrategy().selectRoute(List.of(
                route("B", 10, 100, 5, 1), route("A", 20, 200, 5, 2)), request());
        assertEquals("A", d.selectedRoute().id());
        assertTrue(d.explanation().contains("5.00 km"));
    }
    @Test void comparisonAutomaticallyIncludesRegisteredFifthStrategy() {
        var results = new StrategyComparison(DemoData.strategies()).compare(DemoData.routes(), DemoData.standardRequest());
        assertEquals(5, results.size());
        assertEquals("Distance First", results.get(4).strategyName());
        assertEquals("R07", results.get(4).decision().orElseThrow().selectedRoute().id());
    }
}
