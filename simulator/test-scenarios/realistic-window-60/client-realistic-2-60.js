/* eslint-disable @typescript-eslint/no-var-requires */
const dayjs = require('dayjs');
const jobs = require('./jobs-realistic-2-60.json');

const TICK_INTERVAL_IN_S = 2;
const PORT = 3000;

const parseJobs = () => {
    const now = dayjs();

    return jobs.map((dto) => {
        const expectedTick = dto.expectedStartTick;
        const deviation = dto.executionWindowDeviationTicks;

        // for now irrelevant
        const executionTimeLimitInMs = dto.executionTimeLimitInTicks * TICK_INTERVAL_IN_S * 1000;

        // for now just any interval spanning a reasonable amount of time
        const from = now.clone();
        const to = now.add(10, 'minutes');

        const deviationInMS = deviation * TICK_INTERVAL_IN_S * 1000;

        const m = Math.floor((expectedTick * TICK_INTERVAL_IN_S) / 60);
        const s = (expectedTick * TICK_INTERVAL_IN_S) % 60;
        const cycle = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55].map((c) => c + m).join(',');

        const cronPattern = `${s} ${cycle} * * * *`;

        return {
            name: dto.name,
            requestedNodes: dto.requestedNodes,
            from: from.toISOString(),
            to: to.toISOString(),
            cronPattern,
            executionTimeLimitInMs,
            executionWindowDeviationInMs: deviationInMS
        };
    });
};

const parsedJobs = parseJobs();

const sendJobs = async () => {
    for (const job of parsedJobs) {
        console.log('Sending job', job);
        await fetch(`http://localhost:${PORT}/jobs`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(job)
        });
    }
};

sendJobs();

// todo sending immediate jobs every now and then
