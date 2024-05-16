import { Body, Controller, Get, Logger, Post } from '@nestjs/common';
import { ClusterService } from './Cluster.service';
import { StartJobDTO } from './StartJob.dto';

@Controller()
export class ClusterController {
    private readonly logger = new Logger(ClusterService.name);

    constructor(private readonly clusterService: ClusterService) {}

    @Get()
    public getResources() {
        return this.clusterService.getAvailableResources();
    }

    @Post()
    public startJob(@Body() jobInput: StartJobDTO) {
        this.logger.log('Received request to start job: ' + JSON.stringify(jobInput));

        return this.clusterService.startJob(jobInput);
    }
}
