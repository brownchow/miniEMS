# miniEMS 项目分析文档

## 项目概述

**miniEMS** 是一个基于 Spring Boot 的能源管理系统（Energy Management System）演示项目，主要用于采集和处理电池管理系统（BMS）数据。

## 技术栈

| 类别 | 技术 |
|------|------|
| **后端框架** | Spring Boot 4.0.1 + Java 21 |
| **数据库** | PostgreSQL（关系型）、InfluxDB 2.x（时序数据） |
| **缓存** | Redis 7 |
| **消息中间件** | Eclipse Mosquitto（MQTT） |
| **可视化** | Grafana 10.2.3 |
| **通信协议** | WebSocket、MQTT、Modbus TCP |
| **ORM** | MyBatis-Plus |
| **JSON处理** | FastJSON2 |

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Compose 编排                      │
├──────────┬──────────┬──────────┬──────────┬────────────────┤
│PostgreSQL│  Redis   │ Mosquitto│ InfluxDB │    Grafana     │
│  (16)    │  (7)     │   (2)    │  (2.7)   │   (10.2.3)     │
└────┬─────┴────┬─────┴────┬─────┴────┬─────┴───────┬────────┘
     │          │          │          │             │
     ▼          ▼          ▼          ▼             ▼
┌─────────────────────────────────────────────────────────────┐
│                   miniEMS Backend Service                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ MQTT Handler │  │ Telemetry    │  │ Realtime Push    │   │
│  │              │  │ Service      │  │ (WebSocket)      │   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────────┐
              │   Modbus Battery Simulator    │
              │   (模拟BMS设备，通过MQTT发布)  │
              └───────────────────────────────┘
```

## 核心数据流

1. **Modbus Simulator** → Modbus TCP → 模拟BMS数据
2. **Modbus Simulator** → MQTT (`ems/bms/telemetry`) → 消息broker
3. **miniEMS** ← MQTT 订阅 ← 消息broker
4. **miniEMS** → InfluxDB（时序存储）→ Grafana（可视化）
5. **miniEMS** → WebSocket → 前端实时展示

## 核心模块

| 模块 | 文件位置 | 功能 |
|------|----------|------|
| MQTT处理 | `src/main/java/com/sysbreak/ems/mqtt/MqttMessageHandler.java:38` | 接收MQTT消息，触发后续处理 |
| 遥测服务 | `src/main/java/com/sysbreak/ems/service/TelemetryService.java:30` | 将数据写入InfluxDB |
| 告警服务 | `src/main/java/com/sysbreak/ems/service/AlarmService.java` | 检查并触发告警 |
| 实时推送 | `src/main/java/com/sysbreak/ems/service/RealtimePushService.java` | WebSocket实时推送 |
| 数据解析 | `src/main/java/com/sysbreak/ems/service/DeviceDataParseService.java` | 解析BMS遥测数据 |

## 数据模型

**BmsTelemetry** (`src/main/java/com/sysbreak/ems/model/BmsTelemetry.java:12`):

| 字段 | 类型 | 说明 |
|------|------|------|
| `deviceId` | String | 设备ID |
| `soc` | Double | 电池荷电状态 (%) |
| `voltage` | Double | 电压 (V) |
| `temperature` | Double | 温度 (℃) |
| `timestamp` | Long | 采集时间戳 |

## 部署方式

- 完整的 Docker Compose 编排（8个服务）
- 支持一键启动和健康检查
- 前端静态资源在 `target/classes/static/`

## 服务列表

| 服务名 | 镜像 | 端口 | 说明 |
|--------|------|------|------|
| postgres | postgres:16-alpine | 5432 | PostgreSQL数据库 |
| redis | redis:7-alpine | 6379 | Redis缓存 |
| mqtt | eclipse-mosquitto:2 | 1883, 9001 | MQTT消息代理 |
| influxdb | influxdb:2.7 | 8086 | 时序数据库 |
| grafana | grafana/grafana:10.2.3 | 3000 | 数据可视化平台 |
| modbus-simulator | 自定义Dockerfile | 502 | Modbus电池模拟器 |
| miniems | 自定义Dockerfile | 8080 | 后端服务 |

## 项目定位

这是一个**教学/演示性质**的微服务架构示例，展示了：
- MQTT消息收发
- 时序数据库集成
- WebSocket实时通信
- 多容器编排部署
- 能源数据可视化

## 快速开始

```bash
# 使用Docker Compose启动所有服务
docker compose up -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f miniems
```

## 代码更新与重新编译

### Docker Compose 的重新编译行为

**`docker compose up -d` 本身不会自动重新编译**：
- 如果镜像已经存在，Docker Compose 不会重新构建镜像，只会启动容器
- 如果代码被修改了，使用 `docker compose up -d` 不会触发重新编译

### 强制重新编译的方法

**1. 使用 `--build` 标志（推荐）**：
```bash
docker compose up -d --build
```

**2. 或者先构建再启动**：
```bash
docker compose build
docker compose up -d
```

**3. 使用 Docker Compose Watch（推荐用于开发环境）**：
```bash
docker compose up --watch
```
这会自动监听文件变化并触发重新构建，无需手动执行构建命令。

### 开发建议

- **开发时**：使用 `docker compose up --watch` 实现代码修改后自动重新编译
- **部署时**：使用 `docker compose up -d --build` 确保镜像是最新的

## 相关文档

- `compose.yaml` - Docker Compose编排文件
- `DOCKER_DEPLOYMENT.md` - Docker部署说明
- `docker-mirror-setup.md` - Docker镜像加速配置
- `TROUBLESHOOTING.md` - 故障排查指南
