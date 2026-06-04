package com.bjb.pansin.modules.notification.service;

import com.bjb.pansin.common.enums.NotificationChannel;

public interface NotificationGateway {

    NotificationChannel channel();

    void send(String recipient, String subject, String body);
}
