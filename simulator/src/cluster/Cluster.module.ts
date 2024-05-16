import { Module } from '@nestjs/common';
import { ScheduleModule } from '@nestjs/schedule';

import { JobModule } from '../job/Job.module';
import { ClusterController } from './Cluster.controller';
import { ClusterService } from './Cluster.service';

@Module({
    imports: [ScheduleModule.forRoot(), JobModule],
    controllers: [ClusterController],
    providers: [ClusterService]
})
export class ClusterModule {}
