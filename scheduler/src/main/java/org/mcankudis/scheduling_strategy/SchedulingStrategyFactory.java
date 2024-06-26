package org.mcankudis.scheduling_strategy;

public class SchedulingStrategyFactory {
    public enum StrategyId {
        PHI,
        NEXT_POSSIBLE_FIRST,
        NO_STRATEGY
    }

    private SchedulingStrategyFactory() {
    }

    public static SchedulingStrategy getSchedulingStrategy(StrategyId strategyId) {
        switch (strategyId) {
            case PHI:
                return new SchedulingStrategyImplPHI();
            case NEXT_POSSIBLE_FIRST:
                return new SchedulingStrategyImplNextPossibleFirst();
            case NO_STRATEGY:
                return new SchedulingStrategyImplNoStrategy();
            default:
                throw new IllegalArgumentException("Unknown strategy: " + strategyId);
        }
    }
}
