package com.bjb.pansin.modules.mqtt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
@Slf4j
@Configuration
@IntegrationComponentScan
@RequiredArgsConstructor
public class MqttConfig {

    public static final String INBOUND_CHANNEL = "mqttInboundChannel";
    public static final String OUTBOUND_CHANNEL = "mqttOutboundChannel";

    private final MqttProperties props;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setServerURIs(new String[]{props.getBrokerUrl()});
        opts.setCleanSession(props.isCleanSession());
        opts.setKeepAliveInterval(props.getKeepAlive());
        opts.setAutomaticReconnect(true);
        if (props.getUsername() != null && !props.getUsername().isBlank()) {
            opts.setUserName(props.getUsername());
            opts.setPassword(props.getPassword() != null ? props.getPassword().toCharArray() : new char[0]);
        }
        factory.setConnectionOptions(opts);
        return factory;
    }

    @Bean
    public MessageChannel mqttInboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer mqttInbound(MqttPahoClientFactory factory) {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                props.getClientId() + "-in", factory,
                topicArray());
        adapter.setQos(intArrayOf(props.getQos(), topicArray().length));
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setOutputChannel(mqttInboundChannel());
        adapter.setCompletionTimeout(5000);
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = OUTBOUND_CHANNEL)
    public MessageHandler mqttOutbound(MqttPahoClientFactory factory) {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(
                props.getClientId() + "-out", factory);
        handler.setAsync(true);
        handler.setDefaultQos(props.getQos());
        handler.setDefaultRetained(false);
        return handler;
    }

    private String[] topicArray() {
        return new String[]{
                props.getTopics().getVaultOpen(),
                props.getTopics().getVaultClose(),
                props.getTopics().getVaultAlarm(),
                props.getTopics().getVaultEmergency(),
                props.getTopics().getFingerprintScan(),
                props.getTopics().getFingerprintRegister(),
                props.getTopics().getDeviceHeartbeat(),
                props.getTopics().getDeviceStatus()
        };
    }

    private int[] intArrayOf(int v, int n) {
        int[] a = new int[n];
        java.util.Arrays.fill(a, v);
        return a;
    }
}
