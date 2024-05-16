import dayjs from 'dayjs';

export enum ClusterJobStatus {
    PENDING = 'pending',
    COMPLETED = 'completed',
    FAILED = 'failed'
}

export interface ClusterJob {
    executionId: string;
    jobId: string;
    jobName: string;
    status: ClusterJobStatus;
    resourceUsage: {
        requestedNodes: number;
    };
    mockSettings: {
        minTicks: number;
        maxTicks: number;
        finishProbability: number;
    };
    currentTick?: number;
    startedTick: number;
    startLateBySeconds: number;
    createdAt: dayjs.Dayjs;
    finishedAt?: dayjs.Dayjs;
}

export type ClusterJobInput = Pick<ClusterJob, 'resourceUsage' | 'jobName'> & {
    id: string;
    maxTicks: number;
    startLateBySeconds: number;
};
