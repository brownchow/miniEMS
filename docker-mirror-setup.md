# Docker 镜像加速配置指南

## 问题描述

错误信息：
```
failed to fetch anonymous token: Get "https://auth.docker.io/token?...": 
dial tcp 93.179.102.140:443: connectex: A connection attempt failed...
```

**原因**：无法连接到 Docker Hub 下载镜像（国内网络问题）

---

## 解决方案 1: 配置 Docker Desktop 镜像加速（推荐）

### 步骤 1: 打开 Docker Desktop 设置

1. 打开 Docker Desktop
2. 点击右上角的设置图标（齿轮）⚙️
3. 选择 **Docker Engine**

### 步骤 2: 配置镜像源

将以下内容**合并**到现有的 JSON 配置中（不要完全替换）：

```json
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://docker.1panel.live",
    "https://hub.rat.dev"
  ]
}
```

**完整示例配置**（包含其他默认设置）：
```json
{
  "builder": {
    "gc": {
      "defaultKeepStorage": "20GB",
      "enabled": true
    }
  },
  "experimental": false,
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://docker.1panel.live",
    "https://hub.rat.dev"
  ]
}
```

### 步骤 3: 应用并重启

1. 点击 **Apply & Restart** 按钮
2. 等待 Docker Desktop 重启完成（右下角图标变绿）

### 步骤 4: 验证配置

```powershell
# 查看 Docker 配置
docker info | Select-String -Pattern "Registry Mirrors"
```

应该看到类似输出：
```
Registry Mirrors:
  https://docker.m.daocloud.io/
  https://docker.1panel.live/
  https://hub.rat.dev/
```

---

## 解决方案 2: 手动拉取镜像（临时方案）

如果镜像加速器配置后仍然失败，可以手动从国内镜像站拉取：

```powershell
# 方法 A: 使用 DaoCloud 镜像
docker pull docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-21
docker tag docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-21 maven:3.9-eclipse-temurin-21

docker pull docker.m.daocloud.io/library/eclipse-temurin:21-jre-alpine
docker tag docker.m.daocloud.io/library/eclipse-temurin:21-jre-alpine eclipse-temurin:21-jre-alpine

# 方法 B: 使用阿里云镜像（需要注册账号获取加速地址）
# https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors
```

手动拉取所有需要的镜像：

```powershell
# miniEMS 需要的镜像
docker pull docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-21
docker pull docker.m.daocloud.io/library/eclipse-temurin:21-jre-alpine

# 其他服务需要的镜像
docker pull docker.m.daocloud.io/library/postgres:16-alpine
docker pull docker.m.daocloud.io/library/redis:7-alpine
docker pull docker.m.daocloud.io/library/eclipse-mosquitto:2
docker pull docker.m.daocloud.io/library/influxdb:2.7
docker pull docker.m.daocloud.io/grafana/grafana:10.2.3

# 重命名镜像（如果需要）
docker tag docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-21 maven:3.9-eclipse-temurin-21
docker tag docker.m.daocloud.io/library/eclipse-temurin:21-jre-alpine eclipse-temurin:21-jre-alpine
docker tag docker.m.daocloud.io/library/postgres:16-alpine postgres:16-alpine
docker tag docker.m.daocloud.io/library/redis:7-alpine redis:7-alpine
docker tag docker.m.daocloud.io/library/eclipse-mosquitto:2 eclipse-mosquitto:2
docker tag docker.m.daocloud.io/library/influxdb:2.7 influxdb:2.7
```

---

## 解决方案 3: 使用阿里云镜像加速（需注册）

### 步骤 1: 获取专属加速地址

1. 访问 https://cr.console.aliyun.com/
2. 登录阿里云账号（免费注册）
3. 进入 **容器镜像服务** → **镜像加速器**
4. 获取专属加速地址，格式如：`https://xxxxxx.mirror.aliyuncs.com`

### 步骤 2: 配置 Docker Desktop

在 Docker Desktop → Settings → Docker Engine 中添加：

```json
{
  "registry-mirrors": [
    "https://xxxxxx.mirror.aliyuncs.com"
  ]
}
```

替换 `xxxxxx` 为你的专属加速地址。

---

## 解决方案 4: 使用 VPN 或代理

如果你有 VPN 或代理服务：

### 配置 Docker Desktop 使用代理

1. Docker Desktop → Settings → Resources → Proxies
2. 启用 **Manual proxy configuration**
3. 配置 HTTP/HTTPS 代理地址
4. 点击 **Apply & Restart**

---

## 验证和测试

### 测试镜像拉取

```powershell
# 测试拉取一个小镜像
docker pull hello-world

# 如果成功，测试拉取项目需要的镜像
docker pull maven:3.9-eclipse-temurin-21
```

### 清理并重新构建

```powershell
# 清理之前失败的构建缓存
docker builder prune -a -f

# 重新构建
docker compose build --no-cache

# 启动服务
docker compose up -d
```

---

## 常用国内镜像源

| 镜像源 | 地址 | 说明 |
|--------|------|------|
| DaoCloud | https://docker.m.daocloud.io | 免费，稳定 |
| 1Panel | https://docker.1panel.live | 免费 |
| Rat Dev | https://hub.rat.dev | 免费 |
| 阿里云 | https://xxxxxx.mirror.aliyuncs.com | 需注册，专属地址 |
| 腾讯云 | https://mirror.ccs.tencentyun.com | 免费 |
| 网易云 | https://hub-mirror.c.163.com | 免费 |

---

## Maven 依赖下载加速（可选）

如果 Maven 依赖下载也很慢，创建文件 `settings.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <name>Aliyun Maven</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

然后修改 Dockerfile，在构建阶段挂载这个配置文件。

---

## 推荐流程

1. ✅ **首选**：配置 Docker Desktop 镜像加速器（解决方案 1）
2. ✅ **备选**：如果仍失败，手动拉取镜像（解决方案 2）
3. ✅ **可选**：注册阿里云获取专属加速（解决方案 3）
4. ✅ **最后**：使用 VPN（解决方案 4）

---

## 故障排查

### 镜像源不工作？

```powershell
# 测试镜像源连通性
curl https://docker.m.daocloud.io/v2/

# 应该返回类似：
# {"errors":[{"code":"UNAUTHORIZED",...}]}
# 这表示镜像源可访问
```

### 仍然无法拉取？

1. 尝试多个镜像源
2. 检查防火墙设置
3. 确认网络连接正常
4. 查看 Docker Desktop 日志

---

## 配置完成后

```powershell
# 1. 重启 Docker Desktop
# 2. 验证配置
docker info

# 3. 清理缓存
docker builder prune -a -f

# 4. 重新构建
cd d:\SmartEMS\miniEMS
.\start-docker.ps1
```
