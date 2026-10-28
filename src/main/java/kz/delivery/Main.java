package kz.delivery;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        Map<String, Runnable> modes = Map.of("--compare", Main::compare, "--switch", Main::switching,
                "--auto", Main::automatic, "--failures", Main::failures);
        if (args.length == 0) {
            compare(); switching(); automatic(); failures();
        } else if (args.length == 1 && modes.containsKey(args[0])) {
            modes.get(args[0]).run();
        } else if (args.length == 1 && args[0].equals("--help")) {
            System.out.println("Usage: java -jar target/smart-delivery.jar [--compare|--switch|--auto|--failures|--help]");
        } else {
            System.err.println("Unknown arguments. Use --help.");
            System.exit(2);
        }
    }

    private static void compare() {
        System.out.println("=== COMPARISON: REQUEST #105 (same candidates and constraints) ===");
        for (ComparisonResult result : new StrategyComparison(DemoData.strategies())
                .compare(DemoData.routes(), DemoData.standardRequest())) {
            result.decision().ifPresent(Main::printDecision);
            result.failure().ifPresent(System.out::println);
        }
    }

    private static void switching() {
        System.out.println("\n=== RUNTIME SWITCHING: one RoutePlanner, three strategy changes ===");
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        printCompact(planner.plan(DemoData.routes(), DemoData.standardRequest()));
        List<RouteStrategy> replacements = List.of(new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(), new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2));
        for (RouteStrategy replacement : replacements) {
            planner.setStrategy(replacement);
            printCompact(planner.plan(DemoData.routes(), DemoData.standardRequest()));
        }
        System.out.println("Same Fastest algorithm, budget reduced from 10000 to 2500 KZT:");
        RouteStrategy fastest = new FastestRouteStrategy();
        printCompact(fastest.selectRoute(DemoData.routes(), DemoData.standardRequest()));
        printCompact(fastest.selectRoute(DemoData.routes(),
                DemoData.request("budget-change", DeliveryPriority.STANDARD, 2500, 120, 20, false)));
    }

    private static void automatic() {
        System.out.println("\n=== AUTOMATIC STRATEGY SELECTION ===");
        StrategySelector selector = DemoData.selector();
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        List<DeliveryRequest> requests = List.of(
                DemoData.request("A", DeliveryPriority.URGENT, 9000, 30, 20, true),
                DemoData.request("B", DeliveryPriority.ECONOMY, 2000, 100, 20, false),
                DemoData.request("C", DeliveryPriority.STANDARD, 5000, 100, 1, true),
                DemoData.standardRequest());
        for (DeliveryRequest request : requests) {
            planner.setStrategy(selector.select(request));
            System.out.printf(Locale.ROOT, "Request %s (%s, eco=%s): ", request.id(), request.priority(), request.ecoPreferred());
            printCompact(planner.plan(DemoData.routes(), request));
        }
    }

    private static void failures() {
        System.out.println("\n=== EXPLICIT FAILURE SCENARIOS ===");
        expectFailure("Empty candidate list", () -> new RoutePlanner(new FastestRouteStrategy())
                .plan(List.of(), DemoData.standardRequest()));
        expectFailure("No feasible route", () -> new RoutePlanner(new CheapestRouteStrategy())
                .plan(DemoData.routes(), DemoData.request("impossible", DeliveryPriority.STANDARD, 1, 1, 0, false)));
        expectFailure("Invalid balanced weights", () -> new BalancedRouteStrategy(0.8, 0.8, 0, 0));
        expectFailure("Negative request budget", () -> DemoData.request("invalid", DeliveryPriority.STANDARD, -1, 60, 10, false));
    }

    private static void expectFailure(String title, Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected failure: " + title);
        } catch (NoSuitableRouteException exception) {
            System.out.println(title + ": " + exception.getMessage());
        } catch (IllegalArgumentException exception) {
            System.out.println(title + ": " + exception.getMessage());
        }
    }

    private static void printCompact(RouteDecision d) {
        System.out.printf(Locale.ROOT, "%s -> %s | %d min | %.2f KZT | %.2f km | %.2f kg CO2 | score %.4f%n",
                d.strategyName(), d.selectedRoute().id(), d.metrics().effectiveMinutes(), d.metrics().totalCost(),
                d.metrics().distanceKm(), d.metrics().emissionKg(), d.score());
    }

    private static void printDecision(RouteDecision d) {
        printCompact(d);
        System.out.println("  Reason: " + d.explanation());
        for (RouteAssessment assessment : d.assessments()) {
            if (!assessment.eligible()) {
                System.out.println("  Rejected " + assessment.route().id() + ": " + String.join("; ", assessment.rejectionReasons()));
            } else {
                System.out.printf(Locale.ROOT, "  Candidate %s score %.4f%n", assessment.route().id(),
                        assessment.score().orElseThrow().value());
            }
        }
    }
}
