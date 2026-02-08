# miniEMS 项目分析文档

## 项目概述

**miniEMS** 是一个基于 Spring Boot 的能源管理系统（Energy Management System）演示项目，主要用于采集和处理电池管理系统（BMS）数据。

## 技术栈

| 类别 | 技术 |
|------|------|
| **后端框架** | Spring Boot 4.0.1 + Java 21 |
| **前端框架** | Vue 3 + TypeScript + Element Plus |
| **数据库** | PostgreSQL（关系型）、InfluxDB 2.x（时序数据） |
| **缓存** | Redis 7 |
| **消息中间件** | Eclipse Mosquitto（MQTT） |
| **反向代理** | Nginx |
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
│                   miniEMS Backend Service                   │
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
──────────────────────────────────────────────────────────────
                        Nginx 反向代理
                               │
                               ▼
               ┌───────────────────────────────┐
               │      Vue 3 前端应用           │
               │  /           Dashboard        │
               └───────────────────────────────┘
```

## 部署方式

### 快速启动

```bash
# 1. 构建前端（首次或前端代码修改后）
cd frontend && ./build-frontend.sh

# 2. 启动所有服务
docker compose up -d
```

### 其他命令

```bash
# 停止所有服务
docker compose down

# 重启 nginx（前端更新后）
docker compose restart nginx

# 查看日志
docker compose logs -f
```

## 服务列表

| 服务名 | 镜像 | 端口 | 说明 |
|--------|------|------|------|
| postgres | postgres:16-alpine | 5432 | PostgreSQL数据库 |
| redis | redis:7-alpine | 6379 | Redis缓存 |
| mqtt | eclipse-mosquitto:2 | 1883, 9001 | MQTT消息代理 |
| influxdb | influxdb:2.7 | 8086 | 时序数据库 |
| grafana | grafana/grafana:10.2.3 | 3000 | 数据可视化平台 |
| nginx | nginx:alpine | 80 | 反向代理 + 前端静态资源 |
| modbus-simulator | sysbreak/modbus-simulator:1.0.0 | 502 | Modbus电池模拟器 |
| miniems | 自定义Dockerfile | 8080 | 后端服务 |

## 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端首页 | http://localhost | Vue 3 单页应用 |
| 监控仪表板 | http://localhost/dashboard | Grafana 仪表板嵌入 |
| Grafana | http://localhost/grafana | Grafana 原生界面 |
| 后端API | http://localhost/api | Spring Boot 接口 |

## 代码更新

### 后端更新

```bash
docker compose up -d --build miniems
```

### 前端更新

```bash
./build-frontend.sh && docker compose restart nginx
```

## 相关文档

- `compose.yaml` - Docker Compose编排文件
- `build-frontend.sh` - 前端构建脚本
- `DOCKER_DEPLOYMENT.md` - Docker部署说明
- `docker-mirror-setup.md` - Docker镜像加速配置
- `TROUBLESHOOTING.md` - 故障排查指南
