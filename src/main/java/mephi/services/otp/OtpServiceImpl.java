package mephi.services.otp;

import mephi.entities.otp.*;
import mephi.repository.otpConfig.OtpConfigRepository;
import mephi.repository.otp.OtpRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OtpServiceImpl implements OtpService {
    private final SecureRandom secureRandom = new SecureRandom();
    private final OtpConfigRepository otpConfigRepository;
    private final OtpRepository otpRepository;

    public OtpServiceImpl(
            OtpConfigRepository otpConfigRepository,
            OtpRepository otpRepository
    ) {
        this.otpConfigRepository = otpConfigRepository;
        this.otpRepository = otpRepository;
    }

    private OtpConfig getOtpConfig() {
        return this.otpConfigRepository.getAll().getFirst();
    }

    @Override
    public boolean updateConfig(OtpConfigDto newConfig) {
        return this.otpConfigRepository.update(
            new OtpConfig(
                "1",
                newConfig.length(),
                newConfig.expiration()
            )
        );
    }

    @Override
    public String generate(String userId) {
        OtpConfig otpConfig = this.getOtpConfig();

        int length = otpConfig.length();

        StringBuilder stringBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            stringBuilder.append(secureRandom.nextInt(10));
        }

        String newOtp = stringBuilder.toString();

        this.otpRepository.create(
            new Otp(
                UUID.randomUUID().toString(),
                newOtp,
                Instant.now().toEpochMilli(),
                OtpStatus.active,
                userId,
                UUID.randomUUID().toString()
            )
        );

        return newOtp;
    }

    @Override
    public boolean validate(ValidateOtpDto validateOtpData) {
        List<Otp> otpFound = this.otpRepository.getByCondition(
            String.format(
                "value = '%s' and user_id = '%s'",
                validateOtpData.otp(),
                validateOtpData.userId()
            )
        );

        List<Otp> activeOtp = otpFound.stream().filter((otp) -> otp.status() == OtpStatus.active).toList();
        if (activeOtp.size() != 1) {
            return false;
        }

        Otp currentOtp = activeOtp.getFirst();
        OtpConfig otpConfig = this.getOtpConfig();


        if (Instant.now().toEpochMilli() > currentOtp.createdAt() + otpConfig.expiration() * 1000) {
            this.otpRepository.update(
                new Otp(
                    currentOtp.id(),
                    currentOtp.value(),
                    currentOtp.createdAt(),
                    OtpStatus.expired,
                    currentOtp.userId(),
                    currentOtp.operationId()
                )
            );

            return false;
        }

        return this.otpRepository.update(
            new Otp(
                currentOtp.id(),
                currentOtp.value(),
                currentOtp.createdAt(),
                OtpStatus.used,
                currentOtp.userId(),
                currentOtp.operationId()
            )
        );
    }

    @Override
    public void checkExpiration() {
        List<Otp> activeOtpList = this.otpRepository.getByCondition("status = 'active'");

        if (activeOtpList.isEmpty()) {
            return;
        }

        OtpConfig otpConfig = this.getOtpConfig();

        for (Otp otp : activeOtpList) {
            if (Instant.now().toEpochMilli() > otp.createdAt() + (otpConfig.expiration() * 1000)) {
                this.otpRepository.update(
                    new Otp(
                        otp.id(),
                        otp.value(),
                        otp.createdAt(),
                        OtpStatus.expired,
                        otp.userId(),
                        otp.operationId()
                    )
                );
            }
        }
    }
}
