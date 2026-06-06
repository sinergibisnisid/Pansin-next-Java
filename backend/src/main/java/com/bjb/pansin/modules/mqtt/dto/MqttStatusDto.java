package com.bjb.pansin.modules.mqtt.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MqttStatusDto {

    private boolean connected;
    private String brokerUrl;
    private String clientId;
    private String username;
    private boolean useTls;
    private int qos;
    private int keepAlive;
    private List<TopicInfo> topics;

    @Data
    @Builder
    public static class TopicInfo {
        private String topic;
        private String description;
        private int qos;
    }
}
