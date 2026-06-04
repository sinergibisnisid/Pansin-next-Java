package com.bjb.pansin.modules.livestream.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.mediamtx")
public class MediaMtxProperties {
    private String baseUrl;
    private String username;
    private String password;
    private String snapshotPath;
}
