package org.mcankudis.scheduling_strategy;

import java.util.List;

import org.mcankudis.cluster_resources.ClusterResources;
import org.mcankudis.job.Job;

public interface SchedulingStrategy {
    List<Job> getJobsToStart(List<? extends Job> jobs, ClusterResources clusterResources);
}