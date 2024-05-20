package org.mcankudis.scheduling_strategy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.mcankudis.cluster_resources.ClusterResources;
import org.mcankudis.job.Job;
import org.mcankudis.scheduler_config.SchedulerConfig;

/**
 * This strategy calculates a "PHI" value for each job, based on its resource usage, the current
 * load, time until the job's latest start time, and the expected average load in the next time
 * window. The PHI value is then used to determine whether a job can be started in the current tick.
 */
public class SchedulingStrategyImplPHI implements SchedulingStrategy {
    private SchedulerConfig config;
    
    public List<Job> getJobsToStart(List<? extends Job> jobs, ClusterResources clusterResources, SchedulerConfig config) {
        this.config = config;

        LocalDateTime window = LocalDateTime.now().plusSeconds(config.getWindowSizeInSeconds());

        for (Job job : jobs) {
            System.out.println("    Job: " + job);
        }

        if (jobs.isEmpty()) {
            return List.of();
        }

        int currentLoad = clusterResources.getValue();
        
        int total = calculateTotalResourceUsageOverTime(jobs, window) + currentLoad;

        System.out.println("Calculated total resource usage: " + total);

        int tickLoadAVG = total / config.getTicksPerWindow();

        // todo: use multiple smaller windows to mitigate job clusters skewing the average for the whole window

        System.out.println(
                "Calculated average load per tick in the next " + config.getWindowSizeInSeconds()
                        + " seconds: " + tickLoadAVG);

        List<Job> jobsUnderPHI = new ArrayList<>();

        for (Job job : jobs) {
            if (job.getEarliestStartTime().isAfter(LocalDateTime.now())) {
                continue;
            }

            System.out.println("Job could be started, calculating PHI: " + job + " ");

            int jobLoad = job.getClusterResources().getValue();
            int jobPHI = job.calculatePHI(tickLoadAVG, config.getMaxNodes(), currentLoad);

            // todo: for vw, when max exec time arrives, job needs to be executed regardless of PHI
            boolean canStart = currentLoad + jobLoad <= jobPHI;

            System.out.println("Current load=" + currentLoad + ", job load=" + jobLoad
                    + ", job PHI=" + jobPHI + ", condition=" + canStart);

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

        // multiple jobs can be started in the same tick, as long as they fit into the threshold
        // however, the calculation of the threshold must be improved for that 
        // for now, starting one job per scheduler tick produces decent results

        /*
        List<Job> jobsToExecute = new ArrayList<>(List.of(firstJob));

        int firstJobLoad = firstJob.getClusterResources().getValue();

        int threshold = (int) Math
            .round(tickLoadAVG + ((ConfigLocal.MAX_NODES - tickLoadAVG) * 0.2));

        if(threshold > ConfigLocal.MAX_NODES) threshold = ConfigLocal.MAX_NODES;

        int simulatedLoad = currentLoad + firstJobLoad;

        but we can also execute jobs with later latest start times in this tick, provided they
        fit into a "soft" threshold. If not, they will be handled in the next ticks.
        while (simulatedLoad < threshold) {
            System.out.println(String.format("simulatedLoad: %d, threshold: %d", simulatedLoad, threshold));
            if (jobsUnderPHI.isEmpty()) {
                break;
            }

            Job job = jobsUnderPHI.removeFirst();
            int jobLoad = job.getClusterResources().simpleValue();

            if (simulatedLoad + jobLoad <= threshold) {
                jobsToExecute.add(job);
                simulatedLoad += jobLoad;
            }
        }

        return jobsToExecute;
        */
    }

    private int calculateTotalResourceUsageOverTime(List<? extends Job> jobs, LocalDateTime windowEnd) {
        return jobs.stream()
                .mapToInt(job -> {
                    double jobWindowPercentageBeforeWindowEnd = 1;

                    if (windowEnd.isBefore(job.getLatestStartTime())) {
                        Long fullJobWindow = Duration
                                .between(job.getEarliestStartTime(), job.getLatestStartTime()).abs()
                                .toSeconds();
                        Long toWindowEnd = Duration.between(LocalDateTime.now(), windowEnd).abs()
                                .toSeconds();

                        jobWindowPercentageBeforeWindowEnd = (double) toWindowEnd / fullJobWindow;

                    }

                    int jobTimeLimit = job.getExecutionTimeLimitInMs();
                    int expectedJobTicks = (int) Math.ceil(jobTimeLimit / 1000.0 / config.getTickIntervalInSeconds());

                    return (int) Math
                            .round(job.getClusterResources().getValue()
                                    * jobWindowPercentageBeforeWindowEnd
                                    * expectedJobTicks);
                })
                .sum();
    }
}
