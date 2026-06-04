package com.bjb.pansin.modules.livestream.service;

import com.bjb.pansin.modules.livestream.config.MediaMtxProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaMtxClient {

    private final MediaMtxProperties props;
    private final RestClient http = RestClient.create();

    public byte[] takeSnapshot(String streamPath) {
        try {
            String url = props.getBaseUrl() + "/v3/paths/get/" + streamPath;
            return http.get().uri(url)
                    .headers(this::auth)
                    .retrieve()
                    .body(byte[].class);
        } catch (Exception ex) {
            log.warn("[MediaMTX] snapshot request failed for {}: {}", streamPath, ex.getMessage());
            return null;
        }
    }

    public boolean ping() {
        try {
            http.get().uri(props.getBaseUrl() + "/v3/config/global/get")
                    .headers(this::auth)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void auth(HttpHeaders h) {
        if (props.getUsername() != null && !props.getUsername().isBlank()) {
            String creds = props.getUsername() + ":" + (props.getPassword() != null ? props.getPassword() : "");
            h.set(HttpHeaders.AUTHORIZATION, "Basic " +
                    Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
