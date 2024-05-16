import { dayjs } from '~utils';

export enum JobType {
    CRON = 'CRON',
    IMMEDIATE = 'IMMEDIATE'
}

export interface CronJobData {
    type: JobType.CRON;
    from: dayjs.Dayjs;
    to: dayjs.Dayjs;
    pattern: string;
    status: 'IDLE' | 'RUNNING';

    // Size of the interval in which the job can be executed (added to both sides of the selected time)
    executionWindowDeviationInMs: number;

    nextExecutionOptimalStart: dayjs.Dayjs | null;
    nextExecutionEarliestStart: dayjs.Dayjs | null;
    nextExecutionLatestStart: dayjs.Dayjs | null;
}

export interface ImmediateJobData {
    type: JobType.IMMEDIATE;
    status: 'IDLE' | 'RUNNING' | 'FINISHED';

    // Time in which the job can be executed (f.e. "within the next 15 minutes")
    executionWindowInMs: number;
}

type JobTypeData = CronJobData | ImmediateJobData;

export type JobExecution = {
    startedAt: dayjs.Dayjs;
    finishedAt: dayjs.Dayjs;
};

export interface JobData {
    id: string;
    name: string;
    typeData: JobTypeData;
    requestedNodes: number;
    executionTimeLimitInMs: number;
    executions: JobExecution[];
    createdAt: Date;
    updatedAt: Date;
}
