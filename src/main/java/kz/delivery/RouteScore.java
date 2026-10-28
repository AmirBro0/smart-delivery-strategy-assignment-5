package kz.delivery;

public record RouteScore(double value, String explanation) {
    public RouteScore {
        Checks.nonNegative(value, "Score");
        explanation = Checks.text(explanation, "Score explanation");
    }
}
