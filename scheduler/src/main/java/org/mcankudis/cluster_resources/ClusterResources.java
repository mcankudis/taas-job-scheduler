package org.mcankudis.cluster_resources;

/**
 * Class encapsulating the resources available in the cluster and all related methods.
 * Currently values are only simple integer (f.e. number of nodes)
 */
public interface ClusterResources {
    public int getValue();

    public int calculateClusterLoadPenalty();

    public String toString();

    public String toJSONString(); 
}
