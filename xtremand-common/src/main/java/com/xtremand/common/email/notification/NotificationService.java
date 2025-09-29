package com.xtremand.common.email.notification;

public interface NotificationService {

    void sendActivationEmail(String to, String activationLink);
}
