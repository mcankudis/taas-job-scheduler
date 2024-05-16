import { Injectable, Logger } from '@nestjs/common';
import { randomUUID } from 'crypto';

import { dayjs } from '~utils';
import { AddCronJobDTO, AddImmediateJobDTO } from './AddJob.dto';
import { JobRepository } from './Job.repository';
import { CronJob } from './domain/CronJob';
import { ImmediateJob } from './domain/ImmediateJob';
import { Job } from './domain/Job';
import { jobToExecutableJobsResponse } from './domain/JobMapper';
import { CronJobData, ImmediateJobData, JobData, JobType } from './domain/JobTypes';

@Injectable()
export class JobService {
    private readonly logger = new Logger(JobService.name);
    constructor(private readonly jobRepository: JobRepository) {}

    public addCronJob(input: AddCronJobDTO) {
        const jobData: JobData & { typeData: CronJobData } = {
            id: randomUUID(),
            name: input.name,
            createdAt: new Date(),
            updatedAt: new Date(),
            executions: [],
            executionTimeLimitInMs: input.executionTimeLimitInMs,
            requestedNodes: input.requestedNodes,
            typeData: {
                type: JobType.CRON,
                from: dayjs(input.from),
                to: dayjs(input.to),
                status: 'IDLE',
                executionWindowDeviationInMs: input.executionWindowDeviationInMs,
                pattern: input.cronPattern,
                nextExecutionOptimalStart: null,
                nextExecutionEarliestStart: null,
                nextExecutionLatestStart: null
            }
        };

        this.logger.log(`Adding CronJob`, JSON.stringify(jobData, null, 4));

        const job = new CronJob(jobData);

        this.jobRepository.create(job);
    }

    public addImmediateJob(input: AddImmediateJobDTO) {
        const jobData: JobData & { typeData: ImmediateJobData } = {
            id: randomUUID(),
            name: input.name,
            createdAt: new Date(),
            updatedAt: new Date(),
            executions: [],
            executionTimeLimitInMs: input.executionTimeLimitInMs,
            requestedNodes: input.requestedNodes,
            typeData: {
                type: JobType.IMMEDIATE,
                status: 'IDLE',
                executionWindowInMs: input.executionWindowInMs
            }
        };

        this.logger.log(`Adding ImmediateJob`, JSON.stringify(jobData, null, 4));

        const job = new ImmediateJob(jobData);

        this.jobRepository.create(job);
    }

    public getJobsAbleToStartBefore(time: dayjs.Dayjs) {
        const jobs = this.jobRepository.findJobsAbleToStartBefore(time);
        return jobs.map(jobToExecutableJobsResponse);
    }

    public getJobById(id: string): Job {
        return this.jobRepository.getById(id);
    }

    public startJob(id: string) {
        const job = this.jobRepository.getById(id);
        job.start();
    }

    public finishJob(id: string, startedAt: dayjs.Dayjs, finishedAt: dayjs.Dayjs) {
        const job = this.jobRepository.getById(id);

        job.finish(startedAt, finishedAt);
    }
}
