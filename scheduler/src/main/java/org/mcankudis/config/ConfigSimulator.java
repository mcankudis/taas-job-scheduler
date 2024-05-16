package org.mcankudis.config;

public class ConfigSimulator {
    public static final int MAX_NODES = 100;
    public static final int TICK_INTERVAL_IN_S = 2;
    public static final int WINDOW_SIZE_IN_S = 60;
    public static final int TICKS_PER_WINDOW = WINDOW_SIZE_IN_S / TICK_INTERVAL_IN_S;

    private ConfigSimulator() {
    }
}
