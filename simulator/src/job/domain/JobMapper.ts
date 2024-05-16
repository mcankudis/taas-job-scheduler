import { Job } from './Job';

export const jobToExecutableJobsResponse = (job: Job) => {
    return {
        id: job.id,
        name: job.name,
        type: job.type,
        status: job.status,
        requestedNodes: job.requestedNodes,
        executionTimeLimitInMs: job.executionTimeLimitInMs,
        // temporary hack to make the java service work
        nextExecutionOptimalStart: job.nextExecutionOptimalStart.add(2, 'h'),
        nextExecutionEarliestStart: job.nextExecutionEarliestStart.add(2, 'h'),
        nextExecutionLatestStart: job.nextExecutionLatestStart.add(2, 'h')
    };
};
