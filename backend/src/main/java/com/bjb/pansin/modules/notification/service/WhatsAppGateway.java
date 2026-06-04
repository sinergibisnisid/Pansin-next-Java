package com.bjb.pansin.modules.notification.service;

import com.bjb.pansin.common.enums.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
public class WhatsAppGateway implements NotificationGateway {

    @Value("${app.notification.whatsapp.provider}")
    private String provider;

    @Value("${app.notification.whatsapp.api-url}")
    private String apiUrl;

    @Value("${app.notification.whatsapp.api-token}")
    private String apiToken;

    @Value("${app.notification.whatsapp.sender:}")
    private String sender;

    private final RestClient http = RestClient.create();

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.WHATSAPP;
    }

    @Override
    public void send(String recipient, String subject, String body) {
        if (apiToken == null || apiToken.isBlank()) {
            log.warn("[WA] token not configured, skipping send to {}", recipient);
            return;
        }

        try {
            switch (provider.toLowerCase()) {
                case "fonnte" -> sendFonnte(recipient, body);
                case "wablas" -> sendWablas(recipient, body);
                case "meta"   -> sendMeta(recipient, body);
                default       -> log.warn("[WA] Unknown provider {}", provider);
            }
        } catch (Exception ex) {
            log.error("[WA] failed to send to {}: {}", recipient, ex.getMessage());
        }
    }

    private void sendFonnte(String to, String message) {
        http.post().uri(apiUrl)
                .header("Authorization", apiToken)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("target=" + to + "&message=" + java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8))
                .retrieve()
                .toBodilessEntity();
    }

    private void sendWablas(String to, String message) {
        http.post().uri(apiUrl)
                .header("Authorization", apiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("phone", to, "message", message))
                .retrieve()
                .toBodilessEntity();
    }

    private void sendMeta(String to, String message) {
        http.post().uri(apiUrl)
                .header("Authorization", "Bearer " + apiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "messaging_product", "whatsapp",
                        "to", to,
                        "type", "text",
                        "text", Map.of("body", message)))
                .retrieve()
                .toBodilessEntity();
    }
}
