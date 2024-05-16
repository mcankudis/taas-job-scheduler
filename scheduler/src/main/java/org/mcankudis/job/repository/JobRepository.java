package org.mcankudis.job.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.mcankudis.job.Job;

public interface JobRepository {
    public List<Job> findJobsAbleToStartBefore(LocalDateTime time);
}
