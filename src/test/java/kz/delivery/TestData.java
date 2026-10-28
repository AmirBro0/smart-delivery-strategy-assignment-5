package kz.delivery;

import java.util.Set;

final class TestData {
    private TestData() {}
    static Route route(String id, int minutes, double cost, double distance, double emissions) {
        return new Route(id, DemoData.ORIGIN, DemoData.DESTINATION, distance, minutes,
                cost, 0, 0, emissions, 0, 0, true, Set.of());
    }
    static DeliveryRequest request(double budget, int deadline, double emissions) {
        return DemoData.request("test", DeliveryPriority.ECONOMY, budget, deadline, emissions, false);
    }
    static DeliveryRequest request() { return request(10000, 200, 100); }
    static String selected(RouteStrategy strategy) {
        return strategy.selectRoute(DemoData.routes(), DemoData.standardRequest()).selectedRoute().id();
    }
}
