package com.sysbreak.ems.mqtt;

import com.sysbreak.ems.model.BmsTelemetry;
import com.sysbreak.ems.service.AlarmService;
import com.sysbreak.ems.service.DeviceDataParseService;
import com.sysbreak.ems.service.RealtimePushService;
import com.sysbreak.ems.service.TelemetryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * MQTT 消息处理入口
 */
@Slf4j
@Component
public class MqttMessageHandler {

    private final DeviceDataParseService parseService;
    private final TelemetryService telemetryService;
    private final AlarmService alarmService;
    private final RealtimePushService realtimePushService;

    public MqttMessageHandler(DeviceDataParseService parseService,
                              TelemetryService telemetryService,
                              AlarmService alarmService,
                              RealtimePushService realtimePushService) {
        this.parseService = parseService;
        this.telemetryService = telemetryService;
        this.alarmService = alarmService;
        this.realtimePushService = realtimePushService;
    }

    /**
     * 订阅我们手动定义的 emsInboundChannel
     */
    @ServiceActivator(inputChannel = "emsInboundChannel")
    public void handleMessage(Message<byte[]> message) {
        try {
            String payload = new String(message.getPayload());
            log.debug("收到 MQTT 消息: topic={}, payload={}", 
                message.getHeaders().get("mqtt_receivedTopic"), payload);

            BmsTelemetry telemetry = parseService.parseBmsTelemetry(payload);
            if (telemetry != null) {
                telemetryService.saveTelemetry(telemetry);
                alarmService.checkAndTrigger(telemetry);
                realtimePushService.push(telemetry);
            }
        } catch (Exception e) {
            log.error("处理消息失败: {}", e.getMessage());
        }
    }
}
