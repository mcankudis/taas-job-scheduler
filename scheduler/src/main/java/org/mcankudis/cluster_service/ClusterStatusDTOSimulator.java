package org.mcankudis.cluster_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClusterStatusDTOSimulator {
    public Integer availableNodes;
    public Integer usedNodes;

    public ClusterStatusDTOSimulator() {
    }

    public ClusterStatusDTOSimulator(String responseBody) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ClusterStatusDTOSimulator response = objectMapper
                    .readValue(responseBody, ClusterStatusDTOSimulator.class);

            this.availableNodes = response.availableNodes;
            this.usedNodes = response.usedNodes;
        } catch (JsonProcessingException e) {
            System.err.println("Failed to parse cluster status response: " + responseBody + e);
            throw e;
        }
    }
}
