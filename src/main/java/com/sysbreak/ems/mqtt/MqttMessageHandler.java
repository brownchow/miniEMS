package com.sysbreak.ems.mqtt;

import com.sysbreak.ems.model.BmsTelemetry;
import com.sysbreak.ems.service.AlarmService;
import com.sysbreak.ems.service.DeviceDataParseService;
import com.sysbreak.ems.service.RealtimePushService;
import com.sysbreak.ems.service.TelemetryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MQTT 消息处理入口
 * <p>
 * 【处理流程】
 * 1. MqttConfig.mqttInbound() 收到 MQTT 消息
 * 2. 直接调用 setHandler() 设置的处理器（即当前类的方法）
 * 3. 当前类解析消息并调用业务服务处理
 *
 * @author sysbreak
 * @since 2026-01-16
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
     * 处理 MQTT 消息
     * <p>
     * 【调用时机】
     * 当 MqttPahoMessageDrivenChannelAdapter 收到消息时，会调用此方法。
     * 这是通过 setHandler() 直接绑定的，不经过消息通道。
     *
     * @param payload MQTT 消息内容（JSON 字符串）
     * @param topic   MQTT 消息主题
     */
    public void handleMessage(String payload, String topic) {
        log.info("[Handler] 开始处理MQTT消息");
        log.info("[Handler]   Topic: {}", topic);
        log.info("[Handler]   Payload: {}", payload);
        try {
            BmsTelemetry telemetry = parseService.parseBmsTelemetry(payload);
            if (telemetry == null) {
                log.warn("[Handler] 解析BMS遥测数据失败，Payload: {}", payload);
                return;
            }
            log.info("[Handler] 解析成功 - DeviceID: {}, SOC: {}, Voltage: {}, Temperature: {}", telemetry.getDeviceId(), telemetry.getSoc(), telemetry.getVoltage(), telemetry.getTemperature());

            log.info("[Handler] 步骤1/3 - 保存遥测数据到InfluxDB...");
            telemetryService.saveTelemetry(telemetry);

            log.info("[Handler] 步骤2/3 - 检查告警...");
            alarmService.checkAndTrigger(telemetry);

            log.info("[Handler] 步骤3/3 - 推送实时数据...");
            realtimePushService.push(telemetry);

            log.info("[Handler] 消息处理完成，DeviceID: {}", telemetry.getDeviceId());
        } catch (Exception e) {
            log.error("[Handler] 处理消息异常: {}", e.getMessage(), e);
        }
    }
}
