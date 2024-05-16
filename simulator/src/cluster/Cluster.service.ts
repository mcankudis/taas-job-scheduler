import { HttpException, Injectable, Logger } from '@nestjs/common';
import { Cron } from '@nestjs/schedule';
import { randomUUID } from 'crypto';

import { JobService } from '~job';
import { MAX_NODES, Reporting, TICK_INTERVAL_IN_S, dayjs } from '~utils';
import { ClusterJob, ClusterJobInput, ClusterJobStatus } from './ClusterJob';
import { StartJobDTO } from './StartJob.dto';

@Injectable()
export class ClusterService {
    private readonly logger = new Logger(ClusterService.name);
    private readonly reporting = new Reporting();

    private runningJobs: ClusterJob[] = [];
    public availableNodes = MAX_NODES;
    public currentTick = 0;

    constructor(private readonly jobService: JobService) {
        this.reporting.createFiles();
    }

    @Cron(`*/${TICK_INTERVAL_IN_S} * * * * *`)
    public tick() {
        this.currentTick++;

        this.logger.debug(`[Tick ${this.currentTick}] Available nodes: ` + this.availableNodes);
        this.logger.debug('Running jobs: ' + JSON.stringify(this.runningJobs));

        for (const job of this.runningJobs) {
            if (job.status === ClusterJobStatus.PENDING) {
                this.jobTick(job);
            }
        }

        this.reporting.appendLoadToFile(
            this.currentTick,
            MAX_NODES - this.availableNodes,
            this.availableNodes
        );
    }

    public getAvailableResources(): {
        availableNodes: number;
        usedNodes: number;
    } {
        return {
            availableNodes: this.availableNodes,
            usedNodes: MAX_NODES - this.availableNodes
        };
    }

    public startJob({ id }: StartJobDTO) {
        const job = this.jobService.getJobById(id);

        if (!job) {
            throw new HttpException('Job not found', 404);
        }

        if (job.status === 'RUNNING') {
            throw new HttpException('Job is already running', 400);
        }

        const now = dayjs();
        const latestStart = job.nextExecutionLatestStart;
        const startLateBySeconds = now.isAfter(latestStart) ? now.diff(latestStart, 'seconds') : 0;

        this.jobService.startJob(job.id);

        const jobToExecute: ClusterJobInput = {
            id: job.id,
            jobName: job.name,
            maxTicks: job.executionTimeLimitInMs / TICK_INTERVAL_IN_S / 1000,
            resourceUsage: {
                requestedNodes: job.requestedNodes
            },
            startLateBySeconds
        };

        this.startJobExecution(jobToExecute);
    }

    private startJobExecution(jobInput: ClusterJobInput): ClusterJob {
        const { minTicks, maxTicks, finishProbability } = this.generateMockTicks(jobInput.maxTicks);

        const job: ClusterJob = {
            jobName: jobInput.jobName,
            executionId: randomUUID(),
            jobId: jobInput.id,
            status: ClusterJobStatus.PENDING,
            resourceUsage: {
                requestedNodes: jobInput.resourceUsage.requestedNodes
            },
            mockSettings: {
                minTicks,
                maxTicks,
                finishProbability
            },
            startedTick: this.currentTick,
            startLateBySeconds: jobInput.startLateBySeconds,
            currentTick: 0,
            createdAt: dayjs()
        };

        this.runningJobs.push(job);

        this.availableNodes -= jobInput.resourceUsage.requestedNodes;

        if (this.availableNodes < 0) {
            this.logger.error('Insufficient resources you donut!');
        }

        return job;
    }

    private jobTick(job: ClusterJob) {
        job.currentTick++;

        if (job.currentTick < job.mockSettings.minTicks) return;

        if (job.currentTick >= job.mockSettings.maxTicks) {
            return this.finishJob(job);
        }

        if (Math.random() < job.mockSettings.finishProbability) {
            return this.finishJob(job);
        }
    }

    private finishJob(job: ClusterJob) {
        this.logger.debug(`Finishing execution ${job.executionId} of job ${job.jobId}`);

        this.runningJobs = this.runningJobs.filter((j) => j.executionId !== job.executionId);

        job.status = ClusterJobStatus.COMPLETED;
        job.finishedAt = dayjs();

        this.availableNodes += job.resourceUsage.requestedNodes;

        this.jobService.finishJob(job.jobId, job.createdAt, job.finishedAt);

        this.reporting.appendExecutionToFile(job);
    }

    private generateMockTicks(maxTicks: number) {
        const minTicks = maxTicks / 2;
        const finishProbability = Math.random();

        return {
            minTicks,
            maxTicks,
            finishProbability
        };
    }
}
