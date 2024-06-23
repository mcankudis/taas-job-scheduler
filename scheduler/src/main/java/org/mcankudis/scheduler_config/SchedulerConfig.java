package org.mcankudis.scheduler_config;

import java.time.format.DateTimeFormatter;

public interface SchedulerConfig {
    public int getMaxNodes();
    public int getTickIntervalInSeconds();
    public int getWindowSizeInSeconds();
    public int getTicksPerWindow();
    public DateTimeFormatter getLogDateTimeFormatter();
}
