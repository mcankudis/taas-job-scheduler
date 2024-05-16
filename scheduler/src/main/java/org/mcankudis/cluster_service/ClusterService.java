package org.mcankudis.cluster_service;

public interface ClusterService<JobT, ClusterStatusT> {

    public ClusterStatusT getClusterStatus() throws Exception;

    public void startJob(JobT job) throws Exception;
}
