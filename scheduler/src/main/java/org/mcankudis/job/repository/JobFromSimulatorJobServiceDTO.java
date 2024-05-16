package org.mcankudis.job.repository;

import org.mcankudis.cluster_resources.ClusterResources;
import org.mcankudis.cluster_resources.ClusterResourcesImplSimulator;

public class JobFromSimulatorJobServiceDTO {
    public String id;
    public String name;
    public String type;
    public String status;
    public Integer requestedNodes;
    public Integer executionTimeLimitInMs;
    public String nextExecutionOptimalStart;
    public String nextExecutionEarliestStart;
    public String nextExecutionLatestStart;

    public JobFromSimulatorJobServiceDTO() {
    }

    public ClusterResources getRequiredResources() {
        return new ClusterResourcesImplSimulator(this.requestedNodes);
    }

    public String toString() {
        return "JobToScheduleDTO [" +
                "name='" + name + '\'' +
                ", optimalStartDateTime=" + nextExecutionOptimalStart +
                ", earliestStartDateTime=" + nextExecutionEarliestStart +
                ", latestStartDateTime=" + nextExecutionLatestStart +
                ", requestedNodes=" + requestedNodes +
                ']';
    }
}
