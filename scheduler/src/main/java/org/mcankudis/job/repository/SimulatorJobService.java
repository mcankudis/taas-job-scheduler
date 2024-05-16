package org.mcankudis.job.repository;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class SimulatorJobService {
    @ConfigProperty(name = "WEBHOOK_URL")
    String webhookUrl;

    @ConfigProperty(name = "WEBHOOK_TIMEOUT_SECONDS")
    int webhookTimeoutSeconds;

    public String getJobsAbleToStartBefore(LocalDateTime time)
            throws URISyntaxException, InterruptedException, IOException {
        URI uri = new URI(webhookUrl + "/jobs?time=" + time.toString());

        HttpClient client = HttpClient
                .newBuilder()
                .version(Version.HTTP_2)
                .followRedirects(Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(uri)
                .GET()
                .timeout(Duration.ofSeconds(webhookTimeoutSeconds))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        return response.body();
    }
}
