package kz.delivery;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static kz.delivery.TestData.*;

class StrategyTest {
    @Test void fastestIncludesTrafficAndWeatherInsteadOfRawTime() {
        Route a = new Route("A", DemoData.ORIGIN, DemoData.DESTINATION, 5, 10,
                100, 0, 0, 1, 20, 5, true, Set.of());
        Route b = route("B", 30, 100, 5, 1);
        RouteDecision d = new FastestRouteStrategy().selectRoute(List.of(a, b), request());
        assertEquals("B", d.selectedRoute().id());
        assertEquals(30, d.metrics().effectiveMinutes());
    }
    @Test void fastestDemoSelectsExpressRoute() { assertEquals("R02", selected(new FastestRouteStrategy())); }
    @Test void cheapestIncludesDistanceFeesAndPriority() {
        Route a = new Route("A", DemoData.ORIGIN, DemoData.DESTINATION, 10, 20,
                100, 2.5, 25, 1, 0, 0, true, Set.of());
        Route b = route("B", 20, 300, 10, 1);
        DeliveryRequest urgent = DemoData.request("urgent", DeliveryPriority.URGENT, 1000, 100, 10, false);
        RouteDecision d = new CheapestRouteStrategy().selectRoute(List.of(a, b), urgent);
        assertEquals("A", d.selectedRoute().id());
        assertEquals(650, d.metrics().totalCost(), 1e-9);
    }
    @Test void cheapestDoesNotRankByBaseCostAlone() {
        Route a = new Route("A", DemoData.ORIGIN, DemoData.DESTINATION, 100, 20,
                10, 20, 0, 1, 0, 0, true, Set.of());
        assertEquals("B", new CheapestRouteStrategy().selectRoute(
                List.of(a, route("B", 20, 200, 10, 1)), request()).selectedRoute().id());
    }
    @Test void costRoundsOnceUsingDecimalHalfUp() {
        Route a = new Route("A", DemoData.ORIGIN, DemoData.DESTINATION, 0.1, 1,
                0.1, 0.2, 0.005, 0, 0, 0, true, Set.of());
        assertEquals(0.13, RouteMetrics.calculate(a, request()).totalCost(), 1e-9);
    }
    @Test void ecoIncludesEmissionsAndDistance() {
        var routes = List.of(route("A", 20, 100, 100, 1), route("B", 20, 100, 10, 2));
        RouteDecision d = new EcoFriendlyRouteStrategy().selectRoute(routes, request());
        assertEquals("B", d.selectedRoute().id());
        assertEquals(2.5, d.score(), 1e-9);
    }
    @Test void ecoWeightsAreConfigurable() {
        var routes = List.of(route("A", 20, 100, 100, 1), route("B", 20, 100, 10, 2));
        assertEquals("A", new EcoFriendlyRouteStrategy(1, 0).selectRoute(routes, request()).selectedRoute().id());
    }
    @Test void ecoRejectsZeroAndNegativeWeights() {
        assertThrows(IllegalArgumentException.class, () -> new EcoFriendlyRouteStrategy(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new EcoFriendlyRouteStrategy(-1, 1));
    }
    @Test void balancedDemoMakesTradeoffAcrossAllCriteria() {
        RouteDecision d = new BalancedRouteStrategy(.4, .3, .1, .2)
                .selectRoute(DemoData.routes(), DemoData.standardRequest());
        assertEquals("R06", d.selectedRoute().id());
        assertEquals(0.3046, d.score(), 0.0001);
    }
    @Test void balancedTimeOnlyWeightsChooseFastest() {
        assertEquals("R02", selected(new BalancedRouteStrategy(1, 0, 0, 0)));
    }
    @Test void balancedIdenticalMetricsUseZeroNormalizationAndIdTieBreak() {
        RouteDecision d = new BalancedRouteStrategy(.4, .3, .1, .2).selectRoute(
                List.of(route("B", 10, 100, 5, 1), route("A", 10, 100, 5, 1)), request());
        assertEquals("A", d.selectedRoute().id());
        assertEquals(0, d.score());
    }
    @Test void balancedNormalizesOnlyFeasibleRoutes() {
        Route unavailable = new Route("Z", DemoData.ORIGIN, DemoData.DESTINATION, 10, 1000,
                100, 0, 0, 1, 0, 0, false, Set.of());
        RouteDecision d = new BalancedRouteStrategy(.5, .5, 0, 0).selectRoute(List.of(
                route("A", 10, 300, 10, 1), route("B", 30, 100, 10, 1), unavailable), request(10000, 2000, 100));
        assertEquals("A", d.selectedRoute().id());
        assertEquals(.5, d.score(), 1e-9);
    }
    @Test void balancedRejectsInvalidSum() {
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(.4, .4, .4, .4));
    }
    @Test void balancedRejectsNegativeNanAndInfiniteWeights() {
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(-.1, 1.1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(Double.NaN, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(Double.POSITIVE_INFINITY, 0, 0, 0));
    }
    @Test void changingBudgetChangesSameFastestAlgorithmChoice() {
        RouteStrategy strategy = new FastestRouteStrategy();
        assertEquals("R02", selected(strategy));
        assertEquals("R03", strategy.selectRoute(DemoData.routes(),
                DemoData.request("tight", DeliveryPriority.STANDARD, 2500, 120, 20, false)).selectedRoute().id());
    }
    @Test void everyDecisionExplainsWinnerAndRejectedCandidates() {
        for (RouteStrategy strategy : DemoData.strategies()) {
            RouteDecision d = strategy.selectRoute(DemoData.routes(), DemoData.standardRequest());
            assertFalse(d.explanation().isBlank());
            assertEquals(8, d.assessments().size());
            RouteAssessment unavailable = d.assessments().stream().filter(a -> a.route().id().equals("R08")).findFirst().orElseThrow();
            assertTrue(unavailable.rejectionReasons().contains("Route unavailable"));
            assertTrue(unavailable.score().isEmpty());
            assertTrue(d.assessments().stream().filter(RouteAssessment::eligible).allMatch(a -> a.score().isPresent()));
        }
    }
}
