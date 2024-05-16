import { Logger } from '@nestjs/common';
import { appendFile, writeFile } from 'fs';
import { ClusterJob } from '~cluster';

export class Reporting {
    private readonly logger = new Logger(Reporting.name);

    private loadLogFilename: string;
    private executionLogFilename: string;

    public createFiles() {
        const now = new Date();
        const formattedDate = `${now.getFullYear()}-${now.getMonth() + 1}-${now.getDate()}-${now.getHours()}-${now.getMinutes()}-${now.getSeconds()}`;

        this.createExecutionLogFile(formattedDate);
        this.createLoadLogFile(formattedDate);
        this.appendToIndexFile(formattedDate);
    }

    public appendToIndexFile(formattedDate: string) {
        appendFile('./plot/data/index.txt', `${formattedDate}\n`, (err) => {
            if (err) {
                this.logger.error('Error appending to index file');
            }
        });
    }

    public createExecutionLogFile(formattedDate: string) {
        this.executionLogFilename = './plot/data/execution-log-' + formattedDate + '.csv';

        writeFile(
            this.executionLogFilename,
            'jobId,executionId,jobName,startedTick,finishedTick,ticks,startedAt,finishedAt,startLateBy,resourceUsage\n',
            (err) => {
                if (err) {
                    this.logger.error('Error creating execution log file');
                }
            }
        );
    }

    public appendExecutionToFile(job: ClusterJob) {
        appendFile(
            this.executionLogFilename,
            `${job.jobId},${job.executionId},${job.jobName},${job.startedTick},${job.startedTick + job.currentTick},${job.currentTick},${job.createdAt.toISOString()},${job.finishedAt.toISOString()},${job.startLateBySeconds},${job.resourceUsage.requestedNodes}\n`,
            (err) => {
                if (err) {
                    this.logger.error('Error appending execution stats to file');
                }
            }
        );
    }

    public createLoadLogFile(formattedDate: string) {
        this.loadLogFilename = './plot/data/usage-stats-' + formattedDate + '.csv';

        writeFile(this.loadLogFilename, 'timestamp,tick,usedNodes,freeNodes\n', (err) => {
            if (err) {
                this.logger.error('Error creating usage stats file');
            }
        });
    }

    public appendLoadToFile(tick: number, usedNodes: number, freeNodes: number) {
        appendFile(
            this.loadLogFilename,
            `${new Date().toISOString()},${tick},${usedNodes},${freeNodes}\n`,
            (err) => {
                if (err) {
                    this.logger.error('Error appending usage stats to file');
                }
            }
        );
    }
}
