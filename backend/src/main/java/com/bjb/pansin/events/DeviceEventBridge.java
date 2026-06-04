package com.bjb.pansin.events;

import com.bjb.pansin.modules.device.event.DeviceOfflineEvent;
import com.bjb.pansin.modules.fingerprint.event.FingerprintScannedEvent;
import com.bjb.pansin.modules.websocket.service.WebSocketBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceEventBridge {

    private final WebSocketBroadcaster broadcaster;

    @Async("taskExecutor")
    @EventListener
    public void onOffline(DeviceOfflineEvent event) {
        broadcaster.broadcast(WebSocketBroadcaster.TOPIC_DEVICE, event);
    }

    @Async("taskExecutor")
    @EventListener
    public void onFingerprint(FingerprintScannedEvent event) {
        broadcaster.broadcast(WebSocketBroadcaster.TOPIC_DEVICE, event);
    }
}
