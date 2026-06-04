package com.bjb.pansin.modules.mqtt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.mqtt")
public class MqttProperties {

    private String brokerUrl;
    private String clientId;
    private String username;
    private String password;
    private int qos = 1;
    private int keepAlive = 30;
    private boolean cleanSession = true;

    private Topics topics = new Topics();

    @Data
    public static class Topics {
        private String vaultOpen;
        private String vaultClose;
        private String vaultAlarm;
        private String vaultEmergency;
        private String fingerprintScan;
        private String fingerprintRegister;
        private String deviceHeartbeat;
        private String deviceStatus;
        private String maintenanceReminder;
    }
}
