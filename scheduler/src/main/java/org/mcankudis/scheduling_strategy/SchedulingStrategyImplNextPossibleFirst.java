package org.mcankudis.scheduling_strategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.mcankudis.cluster_resources.ClusterResources;
import org.mcankudis.job.Job;
import org.mcankudis.scheduler_config.SchedulerConfig;

/**
 * This is a simple strategy, which starts jobs according to their earliest due date,
 * but only if (and as soon as) cluster resources are available. Returns one job at a time.
 */
public class SchedulingStrategyImplNextPossibleFirst implements SchedulingStrategy {
    public List<Job> getJobsToStart(List<? extends Job> jobs, ClusterResources clusterResources, SchedulerConfig config) {
        LocalDateTime now = LocalDateTime.now();
        
        if (jobs.isEmpty()) {
            return List.of();
        }

        List<Job> jobsSortedByEarliestDate = new ArrayList<>(jobs);
        jobsSortedByEarliestDate.sort((job1, job2) -> job1.getEarliestStartTime().compareTo(job2.getEarliestStartTime()));

        Job firstJob = jobsSortedByEarliestDate.get(0);

        if(firstJob.getEarliestStartTime().isAfter(now)) {
            return List.of();
        }

        if(clusterResources.getValue() + firstJob.getClusterResources().getValue() > config.getMaxNodes()) {
            return List.of();
        }

        return List.of(firstJob);
    }
}
