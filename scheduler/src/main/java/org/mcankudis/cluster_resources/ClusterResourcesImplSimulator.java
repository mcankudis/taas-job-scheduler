package org.mcankudis.cluster_resources;

import org.mcankudis.cluster_service.ClusterStatusDTOSimulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterResourcesImplSimulator implements ClusterResources {
    private static final Logger LOG = LoggerFactory.getLogger(ClusterResourcesImplSimulator.class);

    private int availableNodes;
    private int usedNodes;

    public ClusterResourcesImplSimulator(String responseBody) {
        try {
            ClusterStatusDTOSimulator response = new ClusterStatusDTOSimulator(responseBody);

            this.availableNodes = response.availableNodes;
            this.usedNodes = response.usedNodes;
        } catch (Exception e) {
            LOG.error("Failed to create ClusterResourcesLocal from string: {}", responseBody, e);
            this.availableNodes = 0;
            this.usedNodes = 0;
        }
    }

    public ClusterResourcesImplSimulator(Integer requiredNodes) {
        this.availableNodes = requiredNodes;
        this.usedNodes = requiredNodes;
    }

    public int getValue() {
        return this.usedNodes;
    }

    public int calculateClusterLoadPenalty() {
        return this.usedNodes * this.usedNodes;
    }

    public String toString() {
        return String.format(
                "[ClusterResourcesNodesLocal](Available=%d, Used=%d)",
                this.availableNodes, this.usedNodes);
    }

    public String toJSONString() {
        return String.format("{\"requestedNodes\": %d}", this.usedNodes);
    }
}
