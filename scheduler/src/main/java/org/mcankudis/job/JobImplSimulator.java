package org.mcankudis.job;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;

import org.mcankudis.cluster_resources.ClusterResources;
import org.mcankudis.cluster_service.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobImplSimulator implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(JobImplSimulator.class);

    private String id;
    private String name;
    private ClusterResources clusterResources;
    private LocalDateTime optimalStartTime;
    private LocalDateTime earliestStartTime;
    private LocalDateTime latestStartTime;
    private int executionTimeLimitInMs;

    public JobImplSimulator(
            String id,
            String name,
            ClusterResources clusterResources,
            LocalDateTime optimalStartTime,
            LocalDateTime earliestStartTime,
            LocalDateTime latestStartTime,
            int executionTimeLimitInMs
        ) {
        this.id = id;
        this.name = name;
        this.clusterResources = clusterResources;
        this.optimalStartTime = optimalStartTime;
        this.earliestStartTime = earliestStartTime;
        this.latestStartTime = latestStartTime;
        this.executionTimeLimitInMs = executionTimeLimitInMs;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setEarliestStartTime(LocalDateTime earliestStartTime) {
        this.earliestStartTime = earliestStartTime;
    }

    public void setLatestStartTime(LocalDateTime latestStartTime) {
        this.latestStartTime = latestStartTime;
    }

    public ClusterResources getClusterResources() {
        return clusterResources;
    }

    public LocalDateTime getEarliestStartTime() {
        return earliestStartTime;
    }

    public LocalDateTime getOptimalStartTime() {
        return optimalStartTime;
    }

    public LocalDateTime getLatestStartTime() {
        return latestStartTime;
    }

    public int getExecutionTimeLimitInMs() {
        return executionTimeLimitInMs;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * The PHI of a job is a number indicating a threshold for the resource usage of the job + the
     * current load of the cluster, below which the job can be triggered.
     * 
     * So if the current load is for example 20 nodes and the job requires 10 nodes, the job can be
     * selected for start if the PHI is 30 or more.
     */
    public int calculatePHI(int tickLoadAVG, int maxNodes, int currentClusterLoad) {
        int timeToDeadline = (int) Duration.between(this.latestStartTime, LocalDateTime.now()).abs()
                .toSeconds();

        if (timeToDeadline < 0) {
            return maxNodes;
        }

        int phi = this.clusterResources.getValue();
        LOG.debug("Calculating job PHI. Starting PHI: {}", phi);

        // if the current load is below the expected average, increase the phi accordingly
        // - "oversaving" early will likely lead to overloads leater, so we want to avoid that
        if (currentClusterLoad < tickLoadAVG) {
            int modifier = (tickLoadAVG - currentClusterLoad) / 2;
            LOG.debug(" => Low load, increasing phi by {}", modifier);
            phi += modifier;
        }

        // if the time left until the deadline is less than the execution time limit for the job, increase phi gradually
        int maxExecutionTimeInS = this.executionTimeLimitInMs / 1000;

        if (tickLoadAVG < maxNodes && timeToDeadline < maxExecutionTimeInS) {
            int theoreticallyFreeResources = maxNodes - tickLoadAVG;
            int timeModifier = maxExecutionTimeInS - timeToDeadline;
            int total = theoreticallyFreeResources * timeModifier / maxExecutionTimeInS;
            phi += total;

            LOG.debug(" => Deadline approaching (in {}), increasing phi by {}", timeToDeadline, total);
        }

        LOG.debug(" => Final PHI: {}", phi);

        return phi > maxNodes ? maxNodes : phi;
    }

    public void trigger(ClusterService clusterService) {
        // todo wait for response and act accordingly - only relevant in the standalone version
        try {
            LOG.info("Requesting to start job: {}", this.id);

            clusterService.startJob(this.toRequestBody());

            LOG.info("Job triggered");
        } catch (Exception e) {
            LOG.error(MessageFormat.format("Error invoking HTTP service: {0}", e));
            // todo further handling
        }
    }

    public String toString() {
        String resources = this.clusterResources != null ? this.clusterResources.toString()
                : "{}";

        return String.format(
                "[JobLocal] id: %s, name: %s, resources: %s, earliestStartTime: %s, latestStartTime: %s",
                this.id, this.name, resources, readableDateDiffToNow(this.earliestStartTime),
                readableDateDiffToNow(this.latestStartTime));
    }

    public String toRequestBody() {
        return String.format("{\"id\": \"%s\"}", this.id);
    }

    private String readableDateDiffToNow(LocalDateTime date) {
        LocalDateTime now = LocalDateTime.now();

        int seconds = (int) (now.until(date, java.time.temporal.ChronoUnit.SECONDS));

        if (seconds < 0) {
            return -seconds + "s ago";
        } else {
            return "in " + (seconds) + "s";
        }
    }
}
