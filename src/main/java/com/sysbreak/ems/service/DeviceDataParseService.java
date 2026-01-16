package com.sysbreak.ems.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.sysbreak.ems.model.BmsTelemetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 设备数据解析服务
 *
 * @author sysbreak
 * @since 2026-01-16
 */
@Slf4j
@Service
public class DeviceDataParseService {

    /**
     * 解析 BMS 遥测数据
     */
    public BmsTelemetry parseBmsTelemetry(String payload) {
        try {
            // Fastjson2 推荐的写法（性能更好，也更安全）
            BmsTelemetry telemetry = JSON.parseObject(payload, BmsTelemetry.class);
            // 补充时间戳（如果设备没上报）
            if (telemetry.getTimestamp() == null) {
                telemetry.setTimestamp(System.currentTimeMillis());
            }
            return telemetry;
        } catch (JSONException e) {
            log.error("解析 BMS 数据失败: {}", payload, e);
            return null;
        } catch (Exception e) {
            // 防止其他意外异常
            log.error("解析 BMS 数据发生未知异常: {}", payload, e);
            return null;
        }
    }

}