package org.mcankudis.job.repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.mcankudis.job.Job;
import org.mcankudis.job.JobImplSimulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JobRepositoryImplSimulator implements JobRepository {
    private static final Logger LOG = LoggerFactory.getLogger(JobRepositoryImplSimulator.class);
    
    private ArrayList<Job> jobs = new ArrayList<>();

    @Inject
    private SimulatorJobService jobService;

    public List<Job> findJobsAbleToStartBefore(LocalDateTime time) {
        try {
            String response = this.jobService.getJobsAbleToStartBefore(time);

            ObjectMapper mapper = new ObjectMapper();

            // todo switch to using timestamps
            DateTimeFormatter formatter = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .withZone(ZoneId.of("Europe/Berlin"));

            JobFromSimulatorJobServiceDTO[] jobsFromService = mapper.readValue(response,
                    JobFromSimulatorJobServiceDTO[].class);

            ArrayList<Job> availableJobs = new ArrayList<>();

            for (JobFromSimulatorJobServiceDTO jobFromService : jobsFromService) {
                Job job = new JobImplSimulator(jobFromService.id, jobFromService.name,
                        jobFromService.getRequiredResources(),
                        LocalDateTime.parse(jobFromService.nextExecutionOptimalStart, formatter),
                        LocalDateTime.parse(jobFromService.nextExecutionEarliestStart, formatter),
                        LocalDateTime.parse(jobFromService.nextExecutionLatestStart, formatter), jobFromService.executionTimeLimitInMs);

                availableJobs.add(job);
            }

            this.jobs = availableJobs;
        } catch (Exception e) {
            LOG.error("Error invoking HTTP service: ", e);
        }

        return this.jobs;
    }
}