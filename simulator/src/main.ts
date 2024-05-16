import { NestFactory } from '@nestjs/core';
import { ClusterModule } from './cluster/Cluster.module';

async function bootstrap() {
    const port = process.env.PORT || 3000;

    const app = await NestFactory.create(ClusterModule);

    console.log(`[Cluster Simulator] Listening on port ${port}`);
    await app.listen(port);
}

bootstrap();
