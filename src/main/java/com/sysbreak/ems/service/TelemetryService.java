package com.sysbreak.ems.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.sysbreak.ems.model.BmsTelemetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * 遥测数据服务
 * 负责：时序数据落库
 *
 * @author sysbreak
 * @since 2026-01-16
 */
@Slf4j
@Service
public class TelemetryService {

    @Autowired
    private InfluxDBClient influxDBClient;

    /**
     * 写入 BMS 遥测数据到 InfluxDB
     */
    public void saveTelemetry(BmsTelemetry telemetry) {
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

}
