package com.sysbreak.ems.mqtt;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.sysbreak.ems.model.BmsTelemetry;
import com.sysbreak.ems.websocket.TelemetryWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

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

    private final InfluxDBClient influxDBClient;
    private final TelemetryWebSocketHandler webSocketHandler;

    public MqttMessageHandler(InfluxDBClient influxDBClient,
                              TelemetryWebSocketHandler webSocketHandler) {
        this.influxDBClient = influxDBClient;
        this.webSocketHandler = webSocketHandler;
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
            BmsTelemetry telemetry = parseBmsTelemetry(payload);
            if (telemetry == null) {
                log.warn("[Handler] 解析BMS遥测数据失败，Payload: {}", payload);
                return;
            }
            log.info("[Handler] 解析成功 - DeviceID: {}, SOC: {}, Voltage: {}, Temperature: {}", telemetry.getDeviceId(), telemetry.getSoc(), telemetry.getVoltage(), telemetry.getTemperature());

            log.info("[Handler] 步骤1/3 - 保存遥测数据到InfluxDB...");
            saveTelemetry(telemetry);

            log.info("[Handler] 步骤2/3 - 检查告警...");
            checkAndTriggerAlarm(telemetry);

            log.info("[Handler] 步骤3/3 - 推送实时数据...");
            pushRealtimeData(telemetry);

            log.info("[Handler] 消息处理完成，DeviceID: {}", telemetry.getDeviceId());
        } catch (Exception e) {
            log.error("[Handler] 处理消息异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 解析 BMS 遥测数据
     *
     * @param payload MQTT 消息内容（JSON 字符串）
     * @return 解析后的 BmsTelemetry 对象，解析失败返回 null
     */
    private BmsTelemetry parseBmsTelemetry(String payload) {
        try {
            BmsTelemetry telemetry = JSON.parseObject(payload, BmsTelemetry.class);
            if (telemetry.getTimestamp() == null) {
                telemetry.setTimestamp(System.currentTimeMillis());
            }
            return telemetry;
        } catch (JSONException e) {
            log.error("解析 BMS 数据失败: {}", payload, e);
            return null;
        } catch (Exception e) {
            log.error("解析 BMS 数据发生未知异常: {}", payload, e);
            return null;
        }
    }

    /**
     * 保存遥测数据到 InfluxDB 时序数据库
     *
     * @param telemetry BMS 遥测数据
     */
    private void saveTelemetry(BmsTelemetry telemetry) {
        Point point = Point
                .measurement("bms_telemetry")
                .addTag("deviceId", telemetry.getDeviceId())
                .addField("soc", telemetry.getSoc())
                .addField("voltage", telemetry.getVoltage())
                .addField("temperature", telemetry.getTemperature())
                .time(Instant.ofEpochMilli(telemetry.getTimestamp()), WritePrecision.MS);
        influxDBClient.getWriteApiBlocking().writePoint(point);
        log.debug("写入 InfluxDB 成功: {}", telemetry.getDeviceId());
    }

    /**
     * 检查告警规则并触发告警
     *
     * @param telemetry BMS 遥测数据
     */
    private void checkAndTriggerAlarm(BmsTelemetry telemetry) {
        if (telemetry.getSoc() != null && telemetry.getSoc() < 20) {
            log.warn("【低 SOC 告警】设备: {}", telemetry.getDeviceId());
        }
        if (telemetry.getTemperature() != null && telemetry.getTemperature() > 60) {
            log.warn("【高温告警】设备: {}", telemetry.getDeviceId());
        }
    }

    /**
     * 通过 WebSocket 推送实时数据到前端
     *
     * @param telemetry BMS 遥测数据
     */
    private void pushRealtimeData(BmsTelemetry telemetry) {
        try {
            String json = JSON.toJSONString(telemetry);
            webSocketHandler.broadcast(json);
        } catch (Exception e) {
            log.error("实时推送失败", e);
        }
    }

}
