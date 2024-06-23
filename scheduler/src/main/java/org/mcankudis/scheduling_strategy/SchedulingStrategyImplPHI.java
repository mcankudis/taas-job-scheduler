package org.mcankudis.scheduling_strategy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.mcankudis.cluster_resources.ClusterResources;
import org.mcankudis.job.Job;
import org.mcankudis.scheduler_config.SchedulerConfig;

/**
 * This strategy calculates a "PHI" value for each job, based on its resource
 * usage, the current
 * load, time until the job's latest start time, and the expected average load
 * in the next time
 * window. The PHI value is then used to determine whether a job can be started
 * in the current tick.
 */
public class SchedulingStrategyImplPHI implements SchedulingStrategy {
    private SchedulerConfig config;

    public List<Job> getJobsToStart(List<? extends Job> jobs, ClusterResources clusterResources,
            SchedulerConfig config) {
        this.config = config;

        LocalDateTime now = LocalDateTime.now();

        if (jobs.isEmpty()) {
            return List.of();
        }

        int biggestTickLoadAVG = 0;
        int currentLoad = clusterResources.getValue();

        int smallWindowsToAnalyze = 4;
        int smallWindowSize = config.getWindowSizeInSeconds() / smallWindowsToAnalyze;
        int smallWindowTicks = config.getTicksPerWindow() / smallWindowsToAnalyze;
        DateTimeFormatter formatter = config.getLogDateTimeFormatter();

        for (int i = 1; i <= smallWindowsToAnalyze; i++) {
            LocalDateTime start = now;
            LocalDateTime end = start.plusSeconds(smallWindowSize * i);

            int windowTotalLoad = calculateTotalResourceUsageInWindow(jobs, now, end) + currentLoad;

            int ticksInConsideredWindows = smallWindowTicks * i;
            int windowTickLoadAVG = windowTotalLoad / ticksInConsideredWindows;

            System.out.println(start.format(formatter) + " - " + end.format(formatter) + " | " + "load: "
                    + windowTotalLoad + " avg: " + windowTickLoadAVG);

            if (windowTickLoadAVG > biggestTickLoadAVG) {
                biggestTickLoadAVG = windowTickLoadAVG;
            }
        }

        List<Job> jobsUnderPHI = new ArrayList<>();

        for (Job job : jobs) {
            if (job.getEarliestStartTime().isAfter(LocalDateTime.now())) {
                continue;
            }

            System.out.println("Job could be started, calculating PHI: " + job + " ");

            int jobLoad = job.getClusterResources().getValue();
            int jobPHI = job.calculatePHI(biggestTickLoadAVG, config.getMaxNodes(), currentLoad);

            // todo: for vw, when max exec time arrives, job needs to be executed regardless of PHI
            boolean canStart = currentLoad + jobLoad <= jobPHI;

            if (canStart) {
                jobsUnderPHI.add(job);
            }
        }

        if (jobsUnderPHI.isEmpty()) {
            return List.of();
        }

        jobsUnderPHI.sort((job1, job2) -> job1.getLatestStartTime()
                .compareTo(job2.getLatestStartTime()));

        Job firstJob = jobsUnderPHI.removeFirst();

        return List.of(firstJob);
    }

    private int calculateTotalResourceUsageInWindow(List<? extends Job> jobs, LocalDateTime windowStart,
            LocalDateTime windowEnd) {
        return jobs.stream()
                .mapToInt(job -> calculateJobResourceUsageInWindow(job, windowStart, windowEnd))
                .sum();
    }

    private int calculateJobResourceUsageInWindow(Job job, LocalDateTime windowStart, LocalDateTime windowEnd) {
        double jobWindowPercentageInWindow = this.calculateWindowOverlap(
                windowStart,
                windowEnd,
                job.getEarliestStartTime(),
                job.getLatestStartTime());

        int jobTimeLimit = job.getExecutionTimeLimitInMs();
        int expectedJobTicks = (int) Math.ceil(jobTimeLimit / 1000.0 / config.getTickIntervalInSeconds());

        return (int) Math
                .round(job.getClusterResources().getValue()
                        * jobWindowPercentageInWindow
                        * expectedJobTicks);
    }

    /**
     * Calculates how much of the second window is contained in the first window
     * 
     * @return a value between 0 and 1, where 0 means no overlap and 1 means the
     *         second window is fully contained in the first window
     */
    private double calculateWindowOverlap(LocalDateTime window1Start, LocalDateTime window1End,
            LocalDateTime window2Start, LocalDateTime window2End) {
        if (window1Start.isAfter(window2End) || window1End.isBefore(window2Start)) {
            return 0;
        }

        if (window2Start.isAfter(window1Start) && window2End.isBefore(window1End)) {
            return 1;
        }

        Duration window2Duration = Duration.between(window2Start, window2End);
        Duration overlapDuration;

        if (window2End.isAfter(window1End)) {
            // window1: |------|
            // window2:     |------|
            overlapDuration = Duration.between(window2Start, window1End);
        } else {
            // window1:     |------|
            // window2: |------|
            overlapDuration = Duration.between(window1Start, window2End);
        }

        return (double) overlapDuration.toSeconds() / (double) window2Duration.toSeconds();
    }
}
