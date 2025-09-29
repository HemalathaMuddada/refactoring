package com.xtremand.auth.forgotpassword.service;

import com.xtremand.auth.forgotpassword.dto.ForgotPasswordRequest;
import com.xtremand.auth.forgotpassword.dto.ResetPasswordRequest;

public interface ForgotPasswordService {
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
