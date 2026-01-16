package com.sysbreak.ems.service;

import com.sysbreak.ems.model.BmsTelemetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 告警服务
 */
@Slf4j
@Service
public class AlarmService {

    /**
     * 简单告警规则示例
     */
    public void checkAndTrigger(BmsTelemetry telemetry) {
        if (telemetry.getSoc() != null && telemetry.getSoc() < 20) {
            log.warn("【低 SOC 告警】设备: {}", telemetry.getDeviceId());
        }
        if (telemetry.getTemperature() != null && telemetry.getTemperature() > 60) {
            log.warn("【高温告警】设备: {}", telemetry.getDeviceId());
        }
    }

}
