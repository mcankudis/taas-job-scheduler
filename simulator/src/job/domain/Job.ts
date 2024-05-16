import dayjs from 'dayjs';
import { JobData } from './JobTypes';

export abstract class Job {
    constructor(protected _data: JobData) {}

    get id() {
        return this._data.id;
    }

    get name() {
        return this._data.name;
    }

    get type() {
        return this._data.typeData.type;
    }

    get status() {
        return this._data.typeData.status;
    }

    get requestedNodes() {
        return this._data.requestedNodes;
    }

    get executionTimeLimitInMs() {
        return this._data.executionTimeLimitInMs;
    }

    get nextExecutionOptimalStart(): dayjs.Dayjs | null {
        return null;
    }

    get nextExecutionEarliestStart(): dayjs.Dayjs | null {
        return null;
    }

    get nextExecutionLatestStart(): dayjs.Dayjs | null {
        return null;
    }

    public canStartAt(_time: dayjs.Dayjs) {
        return false;
    }

    public start() {
        this._data.typeData.status = 'RUNNING';
    }

    public finish(_startedAt: dayjs.Dayjs, _finishedAt: dayjs.Dayjs) {}

    public toSummary() {
        return {};
    }
}
