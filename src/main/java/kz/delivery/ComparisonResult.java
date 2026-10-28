package kz.delivery;

import java.util.Objects;
import java.util.Optional;

public record ComparisonResult(String strategyName, Optional<RouteDecision> decision, Optional<String> failure) {
    public ComparisonResult {
        strategyName = Checks.text(strategyName, "Strategy name");
        Objects.requireNonNull(decision);
        Objects.requireNonNull(failure);
        if (decision.isPresent() == failure.isPresent()) {
            throw new IllegalArgumentException("Exactly one of decision or failure must be present");
        }
    }
}
