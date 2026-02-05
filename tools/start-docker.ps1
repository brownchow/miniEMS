# miniEMS Docker 一键启动脚本
# PowerShell Script

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   miniEMS Docker 部署脚本" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 检查 Docker Desktop 是否运行
Write-Host "检查 Docker Desktop 服务..." -ForegroundColor Yellow
$dockerProcess = Get-Process "Docker Desktop" -ErrorAction SilentlyContinue
if ($null -eq $dockerProcess) {
    Write-Host "✗ Docker Desktop 未运行" -ForegroundColor Red
    Write-Host "请先启动 Docker Desktop，然后等待其完全启动（右下角图标变为绿色）" -ForegroundColor Yellow
    exit 1
}
Write-Host "✓ Docker Desktop 进程正在运行" -ForegroundColor Green

# 检查 Docker 引擎是否就绪
Write-Host "检查 Docker 引擎..." -ForegroundColor Yellow
$retries = 0
$maxRetries = 10
while ($retries -lt $maxRetries) {
    try {
        docker info 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ Docker 引擎就绪" -ForegroundColor Green
            break
        }
    } catch {}
    
    $retries++
    if ($retries -eq $maxRetries) {
        Write-Host "✗ Docker 引擎未就绪" -ForegroundColor Red
        Write-Host "请确保：" -ForegroundColor Yellow
        Write-Host "  1. Docker Desktop 已完全启动（右下角图标为绿色）" -ForegroundColor Yellow
        Write-Host "  2. WSL2 已正确安装和配置" -ForegroundColor Yellow
        Write-Host "  3. 在 Docker Desktop 设置中启用了 WSL2 集成" -ForegroundColor Yellow
        exit 1
    }
    
    Write-Host "等待 Docker 引擎启动... ($retries/$maxRetries)" -ForegroundColor Yellow
    Start-Sleep -Seconds 3
}

# 检查 docker-compose
Write-Host "检查 Docker Compose..." -ForegroundColor Yellow
try {
    $composeVersion = docker compose version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Docker Compose 已安装 ($composeVersion)" -ForegroundColor Green
    } else {
        Write-Host "✗ Docker Compose 未安装" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Docker Compose 检查失败" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "开始部署服务..." -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 停止现有服务
Write-Host "停止现有服务..." -ForegroundColor Yellow
docker compose down

# 构建并启动服务
Write-Host ""
Write-Host "构建并启动所有服务（首次启动需要较长时间）..." -ForegroundColor Yellow
docker compose up -d --build

# 等待服务启动
Write-Host ""
Write-Host "等待服务启动完成..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 显示服务状态
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "服务状态" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
docker compose ps

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "服务访问地址" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "miniEMS 后端:    http://localhost:8080" -ForegroundColor Green
Write-Host "miniEMS 前端:    http://localhost:8080/index.html" -ForegroundColor Green
Write-Host "Grafana:        http://localhost:3000 (admin/admin)" -ForegroundColor Green
Write-Host "InfluxDB:       http://localhost:8086 (admin/admin123456)" -ForegroundColor Green
Write-Host "MQTT Broker:    mqtt://localhost:1883" -ForegroundColor Green
Write-Host ""

Write-Host "====================================="-ForegroundColor Cyan
Write-Host "常用命令" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "查看所有日志:     docker compose logs -f" -ForegroundColor Yellow
Write-Host "查看后端日志:     docker compose logs -f miniems" -ForegroundColor Yellow
Write-Host "查看模拟器日志:   docker compose logs -f modbus-simulator" -ForegroundColor Yellow
Write-Host "停止所有服务:     docker compose down" -ForegroundColor Yellow
Write-Host "重启服务:         docker compose restart" -ForegroundColor Yellow
Write-Host ""

Write-Host "部署完成！" -ForegroundColor Green
Write-Host ""

# 询问是否查看日志
$response = Read-Host "是否查看实时日志？(y/n)"
if ($response -eq 'y' -or $response -eq 'Y') {
    docker compose logs -f
}
