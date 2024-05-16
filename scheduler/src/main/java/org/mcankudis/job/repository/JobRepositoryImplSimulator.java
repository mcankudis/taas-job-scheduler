package org.mcankudis.job.repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.mcankudis.job.Job;
import org.mcankudis.job.JobImplSimulator;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JobRepositoryImplSimulator implements JobRepository {
    private ArrayList<Job> jobs = new ArrayList<>();

    @Inject
    private SimulatorJobService jobService;

    public List<Job> findJobsAbleToStartBefore(LocalDateTime time) {
        System.out.println("Finding jobs able to start before: " + time);
        try {
            String response = this.jobService.getJobsAbleToStartBefore(time);

            ObjectMapper mapper = new ObjectMapper();

            DateTimeFormatter formatter = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .withZone(ZoneId.of("Europe/Berlin"));

            JobFromSimulatorJobServiceDTO[] jobsFromService = mapper.readValue(response,
                    JobFromSimulatorJobServiceDTO[].class);

            ArrayList<Job> availableJobs = new ArrayList<>();

            for (JobFromSimulatorJobServiceDTO jobFromService : jobsFromService) {
                System.out.println("Job from service: " + jobFromService);

                Job job = new JobImplSimulator(jobFromService.id, jobFromService.name,
                        jobFromService.getRequiredResources(),
                        LocalDateTime.parse(jobFromService.nextExecutionOptimalStart, formatter),
                        LocalDateTime.parse(jobFromService.nextExecutionEarliestStart, formatter),
                        LocalDateTime.parse(jobFromService.nextExecutionLatestStart, formatter), jobFromService.executionTimeLimitInMs);

                availableJobs.add(job);
            }

            this.jobs = availableJobs;
        } catch (Exception e) {
            System.out.println("Error invoking HTTP service: " + e);
        }

        return this.jobs;
    }
}