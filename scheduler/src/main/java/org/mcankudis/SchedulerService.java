package org.mcankudis;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SchedulerService {
    @Inject
    private ClusterServiceImplSimulator clusterService;

    @Inject
    private JobRepositoryImplSimulator jobRepository;

    private SchedulerConfig config = new SchedulerConfigImplSimulator();

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerService.class);

    private ClusterResources clusterResources;

    private SchedulingStrategy strategy = SchedulingStrategyFactory.getSchedulingStrategy(Strategy.PHI);

    @Scheduled(every = SchedulerConfigImplSimulator.TICK_INTERVAL_IN_S + "s")
    void schedulerTick() {
        LOG.info("Scheduler tick" );

        if (this.clusterResources == null) {
            LOG.info("Cluster resources not available yet");
            return;
        }

        LocalDateTime window = LocalDateTime.now().plusSeconds(config.getWindowSizeInSeconds());

        List<Job> jobs = this.jobRepository.findJobsAbleToStartBefore(window);

        List<Job> jobsToStart = this.strategy.getJobsToStart(jobs, this.clusterResources, this.config);

        if (jobsToStart.isEmpty()) {
            LOG.info("No job to start");
            return;
        }

        for (Job job : jobsToStart) {
            try {
                LOG.info("Requesting to start job: {}", job.getId());

                job.trigger(this.clusterService);
            } catch (Exception e) {
                LOG.error("Error invoking HTTP service", e);
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

            LOG.info("Cluster state: {}", this.clusterResources);
        } catch (Exception e) {
            LOG.error("Error fetching cluster status", e);
        }
    }
}