package mephi.repository.otp;

import mephi.entities.otp.Otp;
import mephi.repository.base.Creatable;
import mephi.repository.base.Updatable;
import mephi.repository.base.WithGetByCondition;

public interface OtpRepository extends
    Creatable<Otp>,
    Updatable<Otp>,
    WithGetByCondition<Otp>
{}
