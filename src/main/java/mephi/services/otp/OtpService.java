package mephi.services.otp;

import mephi.entities.otp.OtpConfigDto;
import mephi.entities.otp.ValidateOtpDto;

public interface OtpService {
    boolean updateConfig(OtpConfigDto newConfig);
    String generate(String userId);
    boolean validate(ValidateOtpDto validateOtpData);
    void checkExpiration();
}
