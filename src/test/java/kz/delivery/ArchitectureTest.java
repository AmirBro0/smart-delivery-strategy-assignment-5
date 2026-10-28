package kz.delivery;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArchitectureTest {
    @Test void sameContextSupportsThreeRuntimeSwitches() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        assertEquals("R02", plan(planner));
        planner.setStrategy(new CheapestRouteStrategy()); assertEquals("R05", plan(planner));
        planner.setStrategy(new EcoFriendlyRouteStrategy()); assertEquals("R05", plan(planner));
        planner.setStrategy(new BalancedRouteStrategy(.4, .3, .1, .2)); assertEquals("R06", plan(planner));
    }
    private String plan(RoutePlanner planner) {
        return planner.plan(DemoData.routes(), DemoData.standardRequest()).selectedRoute().id();
    }
    @Test void plannerAcceptsUnrelatedStrategyImplementationAndDelegatesOnce() {
        RouteDecision expected = new FastestRouteStrategy().selectRoute(DemoData.routes(), DemoData.standardRequest());
        var calls = new AtomicInteger();
        RouteStrategy custom = new RouteStrategy() {
            public String getStrategyName() { return "Custom"; }
            public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
                calls.incrementAndGet(); return expected;
            }
        };
        assertSame(expected, new RoutePlanner(custom).plan(DemoData.routes(), DemoData.standardRequest()));
        assertEquals(1, calls.get());
    }
    @Test void automaticUrgentSelectionOverridesEcoPreference() {
        assertInstanceOf(FastestRouteStrategy.class, DemoData.selector().select(
                DemoData.request("A", DeliveryPriority.URGENT, 10000, 100, 20, true)));
    }
    @Test void automaticEconomySelectsCheapest() {
        assertInstanceOf(CheapestRouteStrategy.class, DemoData.selector().select(
                DemoData.request("B", DeliveryPriority.ECONOMY, 10000, 100, 20, false)));
    }
    @Test void automaticEcoSelectsEnvironmentalAlgorithm() {
        assertInstanceOf(EcoFriendlyRouteStrategy.class, DemoData.selector().select(
                DemoData.request("C", DeliveryPriority.ECO, 10000, 100, 20, false)));
    }
    @Test void automaticStandardSelectsBalanced() {
        assertInstanceOf(BalancedRouteStrategy.class, DemoData.selector().select(DemoData.standardRequest()));
    }
    @Test void ecoPreferenceOverridesEconomyWhenNotUrgent() {
        assertInstanceOf(EcoFriendlyRouteStrategy.class, DemoData.selector().select(
                DemoData.request("D", DeliveryPriority.ECONOMY, 10000, 100, 20, true)));
    }
    @Test void missingSelectorMappingsFailAtConstruction() {
        assertThrows(NullPointerException.class, () -> new StrategySelector(Map.of(DeliveryPriority.URGENT, new FastestRouteStrategy())));
    }
    @Test void selectorMappingCanBeReconfiguredWithoutChangingSelector() {
        var replacement = new CheapestRouteStrategy();
        var selector = new StrategySelector(Map.of(DeliveryPriority.URGENT, replacement,
                DeliveryPriority.ECONOMY, replacement, DeliveryPriority.ECO, replacement, DeliveryPriority.STANDARD, replacement));
        assertSame(replacement, selector.select(DemoData.standardRequest()));
    }
    @Test void comparisonRunsTwoStrategiesOnSameRequest() {
        var results = new StrategyComparison(List.of(new FastestRouteStrategy(), new CheapestRouteStrategy()))
                .compare(DemoData.routes(), DemoData.standardRequest());
        assertEquals(List.of("R02", "R05"), results.stream()
                .map(r -> r.decision().orElseThrow().selectedRoute().id()).toList());
    }
    @Test void comparisonReportsNoRouteForEveryStrategy() {
        var results = new StrategyComparison(List.of(new FastestRouteStrategy(), new CheapestRouteStrategy()))
                .compare(List.of(), DemoData.standardRequest());
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> r.failure().isPresent() && r.decision().isEmpty()));
    }
    @Test void nullStrategiesAndEmptyComparisonAreRejected() {
        assertThrows(NullPointerException.class, () -> new RoutePlanner(null));
        var planner = new RoutePlanner(new FastestRouteStrategy());
        assertThrows(NullPointerException.class, () -> planner.setStrategy(null));
        assertThrows(IllegalArgumentException.class, () -> new StrategyComparison(List.of()));
    }
}
