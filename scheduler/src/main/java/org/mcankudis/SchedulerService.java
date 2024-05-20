package org.mcankudis;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.mcankudis.cluster_resources.ClusterResources;
import org.mcankudis.cluster_resources.ClusterResourcesImplSimulator;
import org.mcankudis.cluster_service.ClusterServiceImplSimulator;
import org.mcankudis.job.Job;
import org.mcankudis.job.repository.JobRepositoryImplSimulator;
import org.mcankudis.scheduler_config.SchedulerConfig;
import org.mcankudis.scheduler_config.SchedulerConfigImplSimulator;
import org.mcankudis.scheduling_strategy.SchedulingStrategy;
import org.mcankudis.scheduling_strategy.SchedulingStrategyFactory;
import org.mcankudis.scheduling_strategy.SchedulingStrategyFactory.Strategy;

@ApplicationScoped
public class SchedulerService {
    @Inject
    private ClusterServiceImplSimulator clusterService;

    @Inject
    private JobRepositoryImplSimulator jobRepository;

    private SchedulerConfig config = new SchedulerConfigImplSimulator();

    private ClusterResources clusterResources;

    private SchedulingStrategy strategy = SchedulingStrategyFactory.getSchedulingStrategy(Strategy.NEXT_POSSIBLE_FIRST);

    @Scheduled(every = SchedulerConfigImplSimulator.TICK_INTERVAL_IN_S + "s")
    void schedulerTick() {
        System.out.println("Scheduler tick");

        if (this.clusterResources == null) {
            System.out.println("Cluster resources not available yet");
            return;
        }

        LocalDateTime window = LocalDateTime.now().plusSeconds(config.getWindowSizeInSeconds());

        List<Job> jobs = this.jobRepository.findJobsAbleToStartBefore(window);

        List<Job> jobsToStart = this.strategy.getJobsToStart(jobs, this.clusterResources, this.config);

        if (jobsToStart.isEmpty()) {
            System.out.println("No job to start");
            return;
        }

        for (Job job : jobsToStart) {
            try {
                System.out.println("Requesting to start job: " + job.getId());

                job.trigger(this.clusterService);
            } catch (Exception e) {
                System.out.println(MessageFormat.format("Error invoking HTTP service: {0}", e));
            }
        }

        int loadPenalty = this.clusterResources.calculateClusterLoadPenalty();

        System.out.format("Load penalty %d", loadPenalty);
    }

    @Scheduled(every = SchedulerConfigImplSimulator.TICK_INTERVAL_IN_S / 2 + "s")
    void fetchClusterStatus() {
        try {
            String response = this.clusterService.getClusterStatus();

            this.clusterResources = new ClusterResourcesImplSimulator(response);

            System.out.println("Cluster state: " + this.clusterResources);
        } catch (Exception e) {
            System.out.println(MessageFormat.format("Error fetching cluster status: {0}", e));
        }
    }
}