package com.bjb.pansin.modules.mqtt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MqttBrokerStatus {

    @Autowired(required = false)
    private MqttPahoClientFactory factory;

    /**
     * Best-effort liveness check. The Spring Integration adapters maintain their own
     * connection internally; we simply reflect that the factory bean is wired.
     */
    public boolean isConnected() {
        return factory != null;
    }
}
