package kz.delivery;

public record OptimizationWeights(double time, double cost, double distance, double emission) {
    public OptimizationWeights {
        for (double weight : new double[]{time, cost, distance, emission}) {
            if (!Double.isFinite(weight) || weight < 0 || weight > 1) {
                throw new IllegalArgumentException("Balanced weights must be finite and between 0 and 1");
            }
        }
        if (Math.abs(time + cost + distance + emission - 1.0) > 1e-9) {
            throw new IllegalArgumentException("Balanced weights must sum to 1.0");
        }
    }
}
