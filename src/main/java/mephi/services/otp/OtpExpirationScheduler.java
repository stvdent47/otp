package mephi.services.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OtpExpirationScheduler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(OtpExpirationScheduler.class);

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final OtpService otpService;

    private final long interval;

    public OtpExpirationScheduler(OtpService otpService, long interval) {
        this.otpService = otpService;
        this.interval = interval;
    }

    public void start() {
        logger.info("service started");

        this.run();

        scheduledExecutorService.scheduleAtFixedRate(
            this,
            interval,
            interval,
            TimeUnit.MINUTES
        );
    }

    public void run() {
        logger.info("run() was called");
        otpService.checkExpiration();
    }

    public void stop() {
        scheduledExecutorService.shutdownNow();

        logger.info("service stopped");
    }
}
