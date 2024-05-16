import { CronExpression, parseExpression } from 'cron-parser';
import { dayjs } from '~utils';
import { Job } from './Job';
import { CronJobData, JobData } from './JobTypes';

export class CronJob extends Job {
    private interval: CronExpression<false> | null = null;
    constructor(protected _data: JobData & { typeData: CronJobData }) {
        super(_data);

        this.calculateNextExecutionTime();
    }

    get nextExecutionOptimalStart(): dayjs.Dayjs | null {
        return this._data.typeData.nextExecutionOptimalStart;
    }

    get nextExecutionEarliestStart(): dayjs.Dayjs | null {
        return this._data.typeData.nextExecutionEarliestStart;
    }

    get nextExecutionLatestStart(): dayjs.Dayjs | null {
        return this._data.typeData.nextExecutionLatestStart;
    }

    public canStartAt(time: dayjs.Dayjs) {
        return (
            this.status === 'IDLE' &&
            this._data.typeData.from.isBefore(dayjs()) &&
            this._data.typeData.to.isAfter(dayjs()) &&
            this._data.typeData.nextExecutionEarliestStart.isBefore(time)
        );
    }

    public finish(startedAt: dayjs.Dayjs, finishedAt: dayjs.Dayjs) {
        this._data.typeData.status = 'IDLE';
        this._data.executions.push({
            startedAt,
            finishedAt
        });
        this.calculateNextExecutionTime();
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

    private calculateNextExecutionTime() {
        if (this._data.typeData.to.isBefore(dayjs())) {
            this._data.typeData.nextExecutionEarliestStart = null;
            this._data.typeData.nextExecutionLatestStart = null;
            return;
        }

        if (!this.interval) this.interval = parseExpression(this._data.typeData.pattern);

        const nextExecution = dayjs(this.interval.next().toDate());

        const nextExecutionEarliestStart = nextExecution.subtract(
            this._data.typeData.executionWindowDeviationInMs,
            'ms'
        );
        const nextExecutionLatestStart = nextExecution.add(
            this._data.typeData.executionWindowDeviationInMs,
            'ms'
        );

        this._data.typeData.nextExecutionOptimalStart = nextExecution;
        this._data.typeData.nextExecutionEarliestStart = nextExecutionEarliestStart;
        this._data.typeData.nextExecutionLatestStart = nextExecutionLatestStart;

        console.log(`${this.name} [${this.id}]`);
        console.log('Current time:', dayjs().format('YYYY-MM-DD HH:mm:ss'));
        console.log('Next execution:', nextExecution.format('YYYY-MM-DD HH:mm:ss'));
        console.log('Earliest start:', nextExecutionEarliestStart.format('YYYY-MM-DD HH:mm:ss'));
        console.log('Latest start:', nextExecutionLatestStart.format('YYYY-MM-DD HH:mm:ss'));
    }
}
