# taas-job-scheduler

This repository contains the code for my bachelor thesis "Avoiding resource usage peaks in an industrial Testing-as-a-Service Application".
It consist of two parts:

- A scheduler
- A simulator

## Scheduler

The scheduler operates on jobs that define an earliest start time, a latest start time, maximum runtime and a resource requirements.
The scheduler tries to schedule the jobs in a way that the resource usage is as constant as possible.
It is written in Java and Quarkus and can be run as a standalone service and communicate with a job service (a service storing and executing the jobs) via REST.

The scheduler leverages abstractions to allow for (semi)easy swapping of specific parts, like the strategy or cluster/jobs services (API adapters). Each part of the system has its own subfolder (like scheduling_strategy, job, etc.) and the contract (interface) is defined in a file without any suffixes. A specific implementation contains a suffix in the name (f.e. JobRepository and JobRepositoryImplSimulator).
Config is a bit of an exception, but it needs to be re-worked anyway.

Scheduling service runs the scheduler ticks and invokes a selected strategy.

## Simulator

The simulator simulates a service managing jobs and a cluster executing the jobs. Via a REST API, it provides data on available jobs and the current resource usage of the cluster. The service accepts jobs and executes them on the simulated cluster when instructed to do so via an API call. The simulated cluster then simulates execution of jobs and resource usage accordingly. It also contains functions for running different test scenarios, gathering results and plotting them onto graphs. Written in TypeScript.

The plots can be viewed by starting any kind of live/web server in the /simulator/plot folder and opening the index.html file in the browser.

## Integration

```mermaid
  ---
title: Parts of the Scheduler
---
classDiagram
    note "green = stable parts \n blue = parts of the simulator \n white = concrete impls"
    note for SchedulerService"<b>Important: the phi strategy must receive jobs \n that have the earliest start date in the analyzed window \n However, a filter can be built in into the VW version"
    note "This diagram is still subject to change as the integration \n into VW is taking place and feedback is given"

    note for SchedulingStrategyImplPhiVW "Implementations for VW will be created in cooperation \n and be based on implementations for simulator, \n but include VW-specific adjustments"
    note for Job "Job interface will be tidied up \n and ClusterResources will be included"

    class SchedulerService
    class ClusterService
    class JobService

    class SchedulingStrategyFactory {
        +getSchedulingStrategy(StrategyId strategyId)
    }

    class SchedulingStrategy {
        +getJobsToStart(availableJobs) List~Job~
    }
    <<interface>> SchedulingStrategy

    class SchedulingStrategy
    <<interface>> SchedulingStrategy
        SchedulingStrategy <|-- SchedulingStrategyImplPhiVW
        SchedulingStrategy <|-- SchedulingStrategyImplPhi

    class Job {
        +getId() String
        +getName() String
        +calculatePHI() int
        +getEarliestStartTime() LocalDateTime
        +getOptimalStartTime() LocalDateTime
        +getLatestStartTime() LocalDateTime
        +getExecutionTimeLimitInMs() int
        +getClusterResources() ClusterResources
    }
    <<interface>> Job
        Job <|-- JobImplSimulator
        Job <|-- JobImplVW

    class SchedulerConfig {
        +getMaxNodes() int
        +getTickIntervalInSeconds() int
        +getWindowSizeInSeconds() int
        +getTicksPerWindow() int
        +getLogDateTimeFormatter() DateTimeFormatter

    }
    <<interface>> SchedulerConfig
        SchedulerConfig <|-- SchedulerConfigImplSimulator
        SchedulerConfig <|-- SchedulerConfigImplVW


    SchedulerService --> ClusterService : read cluster status
    SchedulerService --> JobService : get jobs, start job
    SchedulerService --> SchedulingStrategyFactory : get scheduling strategy
    SchedulerService --> SchedulerConfig : use
    SchedulerService --> SchedulingStrategy : getJobsToStart(availableJobs)
    SchedulerService --> Job : create from data received from JobService

    SchedulingStrategyFactory --> SchedulingStrategy : create
    SchedulingStrategy --> Job : read data, calculatePhi
    SchedulingStrategy --> SchedulerConfig : use


    style SchedulingStrategy fill:#e6ffcc
    style SchedulingStrategyFactory fill:#e6ffcc
    style Job fill:#e6ffcc
    style SchedulerConfig fill:#e6ffcc

    style SchedulerService fill:#a9c4eb
    style ClusterService fill:#a9c4eb
    style JobService fill:#a9c4eb

    style SchedulingStrategyImplPhi fill:#fff
    style SchedulingStrategyImplPhiVW fill:#fff
    style JobImplSimulator fill:#fff
    style JobImplVW fill:#fff
    style SchedulerConfigImplSimulator fill:#fff
    style SchedulerConfigImplVW fill:#fff
```

## TODO

- [ ] Improvements to the strategy:
  - [ ] Adjustment of parameters in formulas
  - [ ] Inclusion of average historical job execution time in calculations
  - [x] Analysis of smaller windows to properly schedule clustered jobs
- [x] Conversion from Service to an library/script that can be integrated into an existing service instead of having to run as a separate service (solved by integration chart above)
- [ ] Reintroduction of evaluation/penalty functions (were used in testing earlier but got removed for now). Since the scheduler is stateless in the end, all of that needs to happen on the simulator side.
- [ ] Better configurability, f.e. option to ignore resources and start the job if latest start time is reached
- [ ] Better logging and error handling
- [ ] Scenarios with immediate jobs coming in
- [ ] Cleanup data from test runs and organize it better
- [ ] Tests
