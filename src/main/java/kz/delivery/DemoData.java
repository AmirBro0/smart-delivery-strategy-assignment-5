package kz.delivery;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DemoData {
    public static final Location ORIGIN = new Location("Warehouse A", 43.2389, 76.8897);
    public static final Location DESTINATION = new Location("Customer B", 43.2500, 76.9500);

    private DemoData() {}

    public static List<Route> routes() {
        return List.of(
                route("R01", 12, 18, 2200, 100, 300, 4.5, 20, 2, true, Set.of()),
                route("R02", 17, 20, 3700, 110, 500, 6.5, 3, 2, true, Set.of()),
                route("R03", 20, 40, 700, 50, 100, 3.8, 8, 2, true, Set.of()),
                route("R04", 15, 32, 1600, 80, 100, 0.8, 5, 1, true, Set.of()),
                route("R05", 10, 55, 500, 20, 0, 0.1, 0, 3, true, Set.of(DeliveryPriority.URGENT)),
                route("R06", 13, 26, 1700, 75, 150, 2.0, 4, 2, true, Set.of()),
                route("R07", 8, 35, 1800, 120, 300, 3.0, 10, 2, true, Set.of()),
                route("R08", 5, 10, 100, 10, 0, 0.05, 0, 0, false, Set.of()));
    }

    private static Route route(String id, double km, int minutes, double base, double perKm,
                               double fees, double co2, int traffic, int weather, boolean available,
                               Set<DeliveryPriority> prohibited) {
        return new Route(id, ORIGIN, DESTINATION, km, minutes, base, perKm, fees,
                co2, traffic, weather, available, prohibited);
    }

    public static DeliveryRequest request(String id, DeliveryPriority priority, double budget,
                                          int deadline, double emissions, boolean eco) {
        return new DeliveryRequest(id, ORIGIN, DESTINATION, priority, budget, deadline, emissions, eco, Set.of());
    }

    public static DeliveryRequest standardRequest() {
        return request("105", DeliveryPriority.STANDARD, 10000, 120, 20, false);
    }

    public static List<RouteStrategy> strategies() {
        return List.of(new FastestRouteStrategy(), new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(), new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2), new DistanceFirstStrategy());
    }

    public static StrategySelector selector() {
        return new StrategySelector(Map.of(
                DeliveryPriority.URGENT, new FastestRouteStrategy(),
                DeliveryPriority.ECONOMY, new CheapestRouteStrategy(),
                DeliveryPriority.ECO, new EcoFriendlyRouteStrategy(),
                DeliveryPriority.STANDARD, new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2)));
    }
}
