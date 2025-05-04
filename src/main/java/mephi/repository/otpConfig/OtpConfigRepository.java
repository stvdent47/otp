package mephi.repository.otpConfig;

import mephi.repository.base.Updatable;
import mephi.repository.base.WithGetAll;

public interface OtpConfigRepository extends
    Updatable<mephi.entities.otp.OtpConfig>,
    WithGetAll<mephi.entities.otp.OtpConfig>
{}
