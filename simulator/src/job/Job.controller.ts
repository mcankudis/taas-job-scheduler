import { Body, Controller, Get, Logger, Post, Query } from '@nestjs/common';
import { dayjs } from '~utils';
import { AddCronJobDTO, AddImmediateJobDTO, isCronJobInput } from './AddJob.dto';
import { JobService } from './Job.service';

@Controller('/jobs')
export class JobController {
    private readonly logger = new Logger(JobService.name);

    constructor(private readonly jobService: JobService) {}

    @Get()
    public getJobsAbleToStartBefore(@Query('time') time: string) {
        const jobs = this.jobService.getJobsAbleToStartBefore(dayjs(time));
        // console.log(jobs);
        return jobs;
    }

    @Post()
    public addJob(@Body() jobInput: AddCronJobDTO | AddImmediateJobDTO) {
        if (isCronJobInput(jobInput)) {
            this.jobService.addCronJob(jobInput);
        } else {
            this.jobService.addImmediateJob(jobInput);
        }
    }
}
