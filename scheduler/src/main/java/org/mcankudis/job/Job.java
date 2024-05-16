package org.mcankudis.job;

import java.time.LocalDateTime;

import org.mcankudis.cluster_resources.ClusterResources;
import org.mcankudis.cluster_service.ClusterService;

public interface Job {

    public String getId();

    public String getName();

    public void setName(String name);

    public void setEarliestStartTime(LocalDateTime earliestStartTime);

    public void setLatestStartTime(LocalDateTime latestStartTime);

    public ClusterResources getClusterResources();

    public LocalDateTime getEarliestStartTime();
    
    public LocalDateTime getOptimalStartTime();

    public LocalDateTime getLatestStartTime();

    public int getExecutionTimeLimitInMs();

    public int calculatePHI(int tickLoadAVG, int maxResources, int currentClusterLoad);

    public void trigger(ClusterService clusterService);

    public String toRequestBody();
}
