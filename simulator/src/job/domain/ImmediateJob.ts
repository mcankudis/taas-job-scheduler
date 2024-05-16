import { dayjs } from '~utils';

import { Job } from './Job';
import { ImmediateJobData, JobData } from './JobTypes';

export class ImmediateJob extends Job {
    constructor(protected _data: JobData & { typeData: ImmediateJobData }) {
        super(_data);
    }

    get nextExecutionOptimalStart() {
        if (this._data.typeData.status === 'FINISHED') {
            return null;
        }
        return dayjs();
    }

    get nextExecutionEarliestStart() {
        if (this._data.typeData.status === 'FINISHED') {
            return null;
        }
        return dayjs();
    }

    get nextExecutionLatestStart() {
        if (this._data.typeData.status === 'FINISHED') {
            return null;
        }
        return dayjs().add(this._data.typeData.executionWindowInMs, 'ms');
    }

    public canStartAt(_time: dayjs.Dayjs) {
        if (this._data.typeData.status === 'FINISHED') {
            return null;
        }
        return this.status === 'IDLE';
    }

    public finish(startedAt: dayjs.Dayjs, finishedAt: dayjs.Dayjs) {
        this._data.typeData.status = 'FINISHED';
        this._data.executions.push({
            startedAt,
            finishedAt
        });
    }

    public toSummary() {
        return {
            id: this.id,
            name: this.name,
            type: this.type,
            status: this.status,
            requestedNodes: this.requestedNodes,
            executionTimeLimitInMs: this.executionTimeLimitInMs,
            executions: this._data.executions
        };
    }
}
