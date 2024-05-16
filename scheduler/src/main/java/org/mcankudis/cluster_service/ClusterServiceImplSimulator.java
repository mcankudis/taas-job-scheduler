package org.mcankudis.cluster_service;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ClusterServiceImplSimulator implements ClusterService<String, String> {
    @ConfigProperty(name = "WEBHOOK_URL")
    String webhookUrl;

    @ConfigProperty(name = "WEBHOOK_TIMEOUT_SECONDS")
    int webhookTimeoutSeconds;

    public void startJob(String body) throws URISyntaxException, RuntimeException {
        HttpClient client = HttpClient.newBuilder().version(Version.HTTP_2)
                .followRedirects(Redirect.NORMAL).build();

        // todo:
        // webhook auth, f.e. .header(URLConstants.API_KEY_NAME, URLConstants.API_KEY_VALUE)
        // retries on timeout
        // handling of exceptions

        BodyPublisher bodyToSend = createRequestBody(body);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(new URI(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(bodyToSend)
                .timeout(Duration.ofSeconds(webhookTimeoutSeconds))
                .build();

        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            String responseBody = response.body();
            System.out.println("POST response" + responseBody);
        } catch (IOException | InterruptedException e) {
            // todo error handling
            e.printStackTrace();
            throw new RuntimeException("Error invoking HTTP service", e);
        }

    }

    @Override
    public String getClusterStatus()
            throws URISyntaxException, InterruptedException, IOException {
        HttpClient client = HttpClient
                .newBuilder()
                .version(Version.HTTP_2)
                .followRedirects(Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(new URI(webhookUrl))
                .GET()
                .timeout(Duration.ofSeconds(webhookTimeoutSeconds))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        return response.body();
    }

    public String getJobStatus(String jobId)
            throws URISyntaxException, InterruptedException, IOException {
        HttpClient client = HttpClient
                .newBuilder()
                .version(Version.HTTP_2)
                .followRedirects(Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(new URI(webhookUrl + "/job/" + jobId))
                .GET()
                .timeout(Duration.ofSeconds(webhookTimeoutSeconds))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        return response.body();
    }

    private BodyPublisher createRequestBody(String body) {
        if (body == null) {
            return HttpRequest.BodyPublishers.noBody();
        }
        return HttpRequest.BodyPublishers.ofString(body);
    }
}
