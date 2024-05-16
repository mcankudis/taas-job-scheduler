import { Module } from '@nestjs/common';
import { JobController } from './Job.controller';
import { JobRepository } from './Job.repository';
import { JobService } from './Job.service';

@Module({
    imports: [],
    controllers: [JobController],
    providers: [JobService, JobRepository],
    exports: [JobService, JobRepository]
})
export class JobModule {}
