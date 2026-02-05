package com.sysbreak.ems.model;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * 电池管理系统（BMS）遥测数据模型
 *
 * @author sysbreak
 * @since 2026-01-16
 */
@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class BmsTelemetry {

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 电池荷电状态（SOC %）
     */
    private Double soc;

    /**
     * 电池电压（V）
     */
    private Double voltage;

    /**
     * 电池温度（℃）
     */
    private Double temperature;

    /**
     * 采集时间戳
     */
    private Long timestamp;

}
