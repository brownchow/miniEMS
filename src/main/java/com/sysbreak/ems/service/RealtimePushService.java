package com.sysbreak.ems.service;

import com.alibaba.fastjson2.JSON;
import com.sysbreak.ems.model.BmsTelemetry;
import com.sysbreak.ems.websocket.TelemetryWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 实时推送服务
 *
 * @author sysbreak
 * @since 2026-01-16
 */
@Slf4j
@Service
public class RealtimePushService {

    @Autowired
    private TelemetryWebSocketHandler handler;

    public void push(BmsTelemetry telemetry) {
        try {
            String json = JSON.toJSONString(telemetry);
            handler.broadcast(json);
        } catch (Exception e) {
            log.error("实时推送失败", e);
        }
    }

}
