import { Injectable } from '@nestjs/common';
import { dayjs } from '~utils';
import { Job } from './domain/Job';

@Injectable()
export class JobRepository {
    private jobs: Job[] = [];

    public getAll(): Job[] {
        return this.jobs;
    }

    public getById(id: string): Job {
        return this.jobs.find((job) => job.id === id);
    }

    public findJobsAbleToStartBefore(time: dayjs.Dayjs): Job[] {
        return this.jobs.filter((job) => job.canStartAt(time));
    }

    public create(job: Job): void {
        this.jobs.push(job);
    }

    public update(job: Job): void {
        const index = this.jobs.findIndex((j) => j.id === job.id);
        this.jobs[index] = job;
    }

    public delete(id: string): void {
        this.jobs = this.jobs.filter((job) => job.id !== id);
    }
}
