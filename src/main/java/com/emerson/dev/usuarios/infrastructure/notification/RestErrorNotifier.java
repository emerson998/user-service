package com.emerson.dev.usuarios.infrastructure.notification;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.emerson.dev.usuarios.application.port.out.ErrorNotifierPort;

@Component
public class RestErrorNotifier implements ErrorNotifierPort {

    private static final Logger log = LoggerFactory.getLogger(RestErrorNotifier.class);

    private final String webhookUrl;
    private final RestClient restClient;

    public RestErrorNotifier(
            @Value("${app.chaos.agent-webhook-url:}") String webhookUrl,
            @Value("${app.chaos.agent-webhook-timeout-ms:2000}") int timeoutMs) {
        this.webhookUrl = webhookUrl;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public void notifyError(String path, String exceptionType, String message) {
        if (!StringUtils.hasText(webhookUrl)) {
            return;
        }
        try {
            IncidentPayload payload = new IncidentPayload("usuarios-service", path, exceptionType, message,
                    Instant.now());
            restClient.post().uri(webhookUrl).body(payload).retrieve().toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Falha ao notificar chaos-fix-agent em {}: {}", webhookUrl, ex.getMessage());
        }
    }

    private record IncidentPayload(String service, String path, String exceptionType, String message,
            Instant timestamp) {
    }
}
