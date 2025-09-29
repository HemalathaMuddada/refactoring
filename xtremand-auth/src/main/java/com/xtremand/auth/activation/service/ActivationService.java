package com.xtremand.auth.activation.service;

import com.xtremand.domain.entity.User;

public interface ActivationService {

    void createActivationTokenAndSendEmail(User user);

    void activateUser(String token);
}
