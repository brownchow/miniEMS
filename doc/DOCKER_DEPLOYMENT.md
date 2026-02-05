# miniEMS Docker 部署指南

## 📋 前置要求

### Windows 11 + WSL2 环境

1. **安装 Docker Desktop for Windows**
   - 下载地址: https://www.docker.com/products/docker-desktop/
   - 确保在设置中启用 WSL2 集成

2. **验证 Docker 安装**
   - 在 powershell 中执行以下命令，确保 Docker 和 Docker Compose 已正确安装：
   ```powershell
   docker --version
   docker-compose --version
   ```

## 🏗️ 项目架构

完整的 Docker Compose 环境包含以下服务：

| 服务 | 端口 | 说明 |
|------|------|------|
| **miniEMS Backend** | 8080 | Spring Boot 后端服务 |
| **PostgreSQL** | 5432 | 关系型数据库 |
| **Redis** | 6379 | 缓存服务 |
| **MQTT (Mosquitto)** | 1883, 9001 | MQTT 消息代理 |
| **InfluxDB** | 8086 | 时序数据库 |
| **Grafana** | 3000 | 数据可视化 |
| **Modbus Simulator** | - | 电池模拟器 |

## 🚀 快速部署

### 1. 构建并启动所有服务

在项目根目录 `d:\SmartEMS\miniEMS` 下执行：

```powershell
# 构建并启动所有服务（首次启动会自动构建镜像）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 2. 单独查看某个服务日志

```powershell
# 查看后端服务日志
docker-compose logs -f miniems

# 查看模拟器日志
docker-compose logs -f modbus-simulator

# 查看 MQTT 日志
docker-compose logs -f mqtt
```

### 3. 停止服务

```powershell
# 停止所有服务
docker-compose down

# 停止并删除所有数据卷（慎用！会清空数据）
docker-compose down -v
```

## 🔍 服务访问地址

启动成功后，可通过以下地址访问各服务：

- **miniEMS 后端**: http://localhost:8080
- **miniEMS 前端**: http://localhost:8080/index.html
- **Grafana**: http://localhost:3000 (默认账号: admin/admin)
- **InfluxDB**: http://localhost:8086 (默认账号: admin/admin123456)
- **MQTT Broker**: mqtt://localhost:1883

## 🔧 常用操作

### 重新构建服务

```powershell
# 重新构建所有服务
docker-compose build

# 重新构建特定服务
docker-compose build miniems
docker-compose build modbus-simulator

# 重新构建并启动
docker-compose up -d --build
```

### 进入容器内部

```powershell
# 进入后端容器
docker exec -it miniems-backend sh

# 进入数据库容器
docker exec -it miniems-postgres psql -U postgres -d mini_ems

# 进入 Redis 容器
docker exec -it miniems-redis redis-cli -a MiniEms@123
```

### 查看容器资源使用情况

```powershell
docker stats
```

### 清理 Docker 资源

```powershell
# 清理未使用的容器
docker container prune

# 清理未使用的镜像
docker image prune

# 清理所有未使用的资源（包括卷）
docker system prune -a --volumes
```

## 🔐 InfluxDB 初始化配置

首次启动后，InfluxDB 会自动初始化：

- **Organization**: ems-org
- **Bucket**: ems
- **Token**: ems-token
- **Username**: admin
- **Password**: admin123456

## 📊 Grafana 配置

### 1. 登录 Grafana
访问 http://localhost:3000，使用默认账号登录（admin/admin）

### 2. 添加 InfluxDB 数据源
1. 点击左侧菜单 **Configuration** → **Data Sources**
2. 点击 **Add data source**
3. 选择 **InfluxDB**
4. 配置如下：
   - **Query Language**: Flux
   - **URL**: http://influxdb:8086
   - **Organization**: ems-org
   - **Token**: ems-token
   - **Default Bucket**: ems
5. 点击 **Save & Test**

## 🧪 测试 MQTT 连接

### 使用 MQTT 客户端测试

```powershell
# 订阅主题（需要安装 mosquitto-clients）
docker exec -it miniems-mqtt mosquitto_sub -t "ems/bms/telemetry" -v

# 发布测试消息
docker exec -it miniems-mqtt mosquitto_pub -t "ems/bms/telemetry" -m '{"deviceId":"TEST","soc":80.5}'
```

## 🐛 故障排查

### 服务启动失败

1. **检查端口占用**
   ```powershell
   netstat -ano | findstr "8080"
   netstat -ano | findstr "5432"
   ```

2. **查看服务日志**
   ```powershell
   docker-compose logs miniems
   ```

3. **检查健康状态**
   ```powershell
   docker-compose ps
   ```

### 数据库连接失败

```powershell
# 检查 PostgreSQL 是否就绪
docker exec -it miniems-postgres pg_isready -U postgres

# 查看数据库日志
docker-compose logs postgres
```

### MQTT 连接失败

```powershell
# 检查 MQTT 服务状态
docker-compose logs mqtt

# 测试 MQTT 连接
docker exec -it miniems-mqtt mosquitto_sub -t '$SYS/#' -C 1
```

## 📝 环境变量配置

如需修改配置，可在 `compose.yaml` 中调整环境变量：

```yaml
miniems:
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/mini_ems
    MQTT_BROKER_URL: tcp://mqtt:1883
    INFLUXDB_URL: http://influxdb:8086
    # ... 其他配置
```

## 🔄 数据持久化

以下数据会持久化存储在 Docker 卷中：
- PostgreSQL 数据
- Redis 数据
- InfluxDB 数据
- Grafana 配置
- MQTT 持久化消息

查看卷信息：
```powershell
docker volume ls
docker volume inspect miniems_postgres_data
```

## 🎯 生产环境建议

1. **修改默认密码**
   - 更新 PostgreSQL、Redis、InfluxDB、Grafana 的默认密码
   
2. **启用 MQTT 认证**
   - 修改 `mosquitto/config/mosquitto.conf`，配置用户名密码

3. **配置资源限制**
   - 在 `compose.yaml` 中为每个服务添加 CPU 和内存限制

4. **启用 HTTPS**
   - 配置 Nginx 反向代理
   - 申请 SSL 证书

5. **备份策略**
   - 定期备份 PostgreSQL 和 InfluxDB 数据

## 📚 相关文档

- [Docker 官方文档](https://docs.docker.com/)
- [Docker Compose 文档](https://docs.docker.com/compose/)
- [Spring Boot Docker 部署](https://spring.io/guides/topicals/spring-boot-docker/)
