package org.mcankudis.scheduling_strategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.mcankudis.cluster_resources.ClusterResources;
import org.mcankudis.job.Job;
import org.mcankudis.scheduler_config.SchedulerConfig;

/**
 * This is a no-strategy, which just starts jobs according to their latest due date
 */
public class SchedulingStrategyImplNoStrategy implements SchedulingStrategy {
    public List<Job> getJobsToStart(List<? extends Job> jobs, ClusterResources clusterResources, SchedulerConfig config) {
        LocalDateTime now = LocalDateTime.now();
        
        if (jobs.isEmpty()) {
            return List.of();
        }

        List<Job> jobsToStart = new ArrayList<>();

        for (Job job : jobs) {
            if (job.getOptimalStartTime().isBefore(now)) {
                jobsToStart.add(job);
            }
        }

        return jobsToStart;
    }
}
