package org.mcankudis.scheduler_config;

public interface SchedulerConfig {
    public int getMaxNodes();
    public int getTickIntervalInSeconds();
    public int getWindowSizeInSeconds();
    public int getTicksPerWindow();
}
