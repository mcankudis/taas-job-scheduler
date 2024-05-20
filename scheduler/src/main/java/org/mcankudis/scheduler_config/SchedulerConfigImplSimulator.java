package org.mcankudis.scheduler_config;

public class SchedulerConfigImplSimulator implements SchedulerConfig {
    // must be public in order to be used in @Scheduled annotations
    public static final int TICK_INTERVAL_IN_S = 2;

    private static final int MAX_NODES = 100;
    private static final int WINDOW_SIZE_IN_S = 60;
    private static final int TICKS_PER_WINDOW = WINDOW_SIZE_IN_S / TICK_INTERVAL_IN_S;

    public int getMaxNodes() {
        return MAX_NODES;
    }

    public int getTickIntervalInSeconds() {
        return TICK_INTERVAL_IN_S;
    }

    public int getWindowSizeInSeconds() {
        return WINDOW_SIZE_IN_S;
    }

    public int getTicksPerWindow() {
        return TICKS_PER_WINDOW;
    }
}
