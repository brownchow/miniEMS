# Docker 故障排查指南

## 问题 1: Docker Desktop 连接失败

### 错误信息
```
error during connect: Get "http://%2F%2F.%2Fpipe%2FdockerDesktopLinuxEngine/...": 
open //./pipe/dockerDesktopLinuxEngine: The system cannot find the file specified.
```

### 解决方案

#### 方法 1: 确保 Docker Desktop 完全启动（推荐）

1. **完全退出 Docker Desktop**
   - 右键点击系统托盘中的 Docker 图标
   - 选择 "Quit Docker Desktop"
   - 等待进程完全退出

2. **重新启动 Docker Desktop**
   - 以管理员身份运行 Docker Desktop
   - 等待右下角图标变为**绿色**（这很重要！）
   - 通常需要 30-60 秒才能完全就绪

3. **验证 Docker 是否就绪**
   ```powershell
   docker info
   docker version
   ```

4. **重新运行部署脚本**
   ```powershell
   .\start-docker.ps1
   ```

#### 方法 2: 检查 WSL2 集成

1. **打开 Docker Desktop 设置**
   - Settings → Resources → WSL Integration

2. **确保启用了 WSL2 集成**
   - 勾选 "Enable integration with my default WSL distro"
   - 勾选你使用的 WSL 发行版（如 Ubuntu）

3. **应用并重启 Docker Desktop**

4. **验证 WSL2 中的 Docker**
   ```bash
   # 在 WSL2 终端中执行
   docker ps
   ```

#### 方法 3: 重置 Docker Desktop（终极方案）

1. **打开 Docker Desktop 设置**
   - Settings → Troubleshoot → Reset to factory defaults

2. **执行重置**
   - 这会删除所有容器、镜像和卷
   - 重置后重新配置 WSL2 集成

3. **重启系统**

---

## 问题 2: compose.yaml 版本警告

### 错误信息
```
the attribute `version` is obsolete, it will be ignored
```

### 解决方案

✅ **已修复**：移除了 `version: '3.8'` 行

新版 Docker Compose 不再需要 version 字段。如果你看到这个警告，说明文件已自动更新。

---

## 问题 3: 服务启动失败

### 检查步骤

1. **查看所有服务状态**
   ```powershell
   docker compose ps
   ```

2. **查看失败服务的日志**
   ```powershell
   # 查看所有日志
   docker compose logs

   # 查看特定服务日志
   docker compose logs miniems
   docker compose logs modbus-simulator
   docker compose logs postgres
   ```

3. **检查端口占用**
   ```powershell
   # 检查常用端口
   netstat -ano | findstr "8080"   # miniEMS
   netstat -ano | findstr "5432"   # PostgreSQL
   netstat -ano | findstr "6379"   # Redis
   netstat -ano | findstr "1883"   # MQTT
   netstat -ano | findstr "8086"   # InfluxDB
   netstat -ano | findstr "3000"   # Grafana
   ```

4. **释放被占用的端口**
   ```powershell
   # 找到占用端口的进程 PID
   netstat -ano | findstr "8080"
   
   # 结束进程（替换 PID）
   taskkill /PID <PID> /F
   ```

---

## 问题 4: 构建镜像失败

### Maven 依赖下载失败

```powershell
# 清理 Docker 缓存并重新构建
docker compose build --no-cache miniems
docker compose build --no-cache modbus-simulator
```

### 网络问题

如果在中国大陆，Maven 下载可能很慢：

1. **配置 Maven 阿里云镜像**
   
   在 `modbus-simulator/pom.xml` 和项目的 `pom.xml` 中都可以添加：
   ```xml
   <repositories>
       <repository>
           <id>aliyun</id>
           <url>https://maven.aliyun.com/repository/public</url>
       </repository>
   </repositories>
   ```

2. **或者使用 Docker 镜像加速**
   
   在 Docker Desktop → Settings → Docker Engine 中添加：
   ```json
   {
     "registry-mirrors": [
       "https://docker.mirrors.ustc.edu.cn"
     ]
   }
   ```

---

## 问题 5: 数据库连接失败

### PostgreSQL 未就绪

```powershell
# 检查 PostgreSQL 健康状态
docker compose ps postgres

# 查看 PostgreSQL 日志
docker compose logs postgres

# 手动测试连接
docker exec -it miniems-postgres psql -U postgres -d mini_ems
```

### InfluxDB 未初始化

```powershell
# 检查 InfluxDB 日志
docker compose logs influxdb

# 手动访问 InfluxDB UI
# 浏览器打开: http://localhost:8086
```

---

## 问题 6: MQTT 连接失败

### 检查 Mosquitto 服务

```powershell
# 查看 MQTT 日志
docker compose logs mqtt

# 测试 MQTT 连接
docker exec -it miniems-mqtt mosquitto_sub -t '$SYS/#' -C 1
```

### 测试发布和订阅

```powershell
# 终端 1: 订阅主题
docker exec -it miniems-mqtt mosquitto_sub -t "ems/bms/telemetry" -v

# 终端 2: 发布测试消息
docker exec -it miniems-mqtt mosquitto_pub -t "ems/bms/telemetry" -m '{"test":"data"}'
```

---

## 常用清理命令

### 停止并删除所有容器

```powershell
docker compose down
```

### 停止并删除所有容器和卷（会清空数据）

```powershell
docker compose down -v
```

### 清理 Docker 系统

```powershell
# 清理未使用的容器
docker container prune -f

# 清理未使用的镜像
docker image prune -a -f

# 清理未使用的卷
docker volume prune -f

# 清理所有未使用的资源
docker system prune -a --volumes -f
```

---

## 验证部署成功

### 1. 检查所有服务状态

```powershell
docker compose ps
```

所有服务应该显示 `Up` 状态。

### 2. 检查服务健康状态

```powershell
# 查看健康状态
docker ps --format "table {{.Names}}\t{{.Status}}"
```

### 3. 测试各服务访问

```powershell
# 测试后端健康检查
curl http://localhost:8080/actuator/health

# 测试 InfluxDB
curl http://localhost:8086/health

# 测试 Grafana
curl http://localhost:3000/api/health
```

### 4. 查看模拟器是否发送数据

```powershell
# 查看模拟器日志，应该看到周期性发送数据
docker compose logs -f modbus-simulator
```

应该看到类似输出：
```
Published battery data: SOC=84.23%, Voltage=52.15V, Current=25.34A, Temp=28.56°C
```

---

## 获取帮助

如果以上方法都无法解决问题：

1. **查看完整日志**
   ```powershell
   docker compose logs > logs.txt
   ```

2. **检查 Docker Desktop 日志**
   - Docker Desktop → Troubleshoot → Get support
   - 导出诊断日志

3. **检查系统资源**
   ```powershell
   docker stats
   ```

4. **验证 WSL2 安装**
   ```powershell
   wsl --list --verbose
   wsl --status
   ```
