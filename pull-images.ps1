# Docker 镜像预拉取脚本
# 用于解决网络问题，提前从国内镜像源拉取所需镜像

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   Docker 镜像预拉取脚本" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 国内镜像源
$MIRROR = "docker.m.daocloud.io"

# 需要的镜像列表
$images = @(
    @{Source="library/maven:3.9-eclipse-temurin-21"; Target="maven:3.9-eclipse-temurin-21"},
    @{Source="library/eclipse-temurin:21-jre-alpine"; Target="eclipse-temurin:21-jre-alpine"},
    @{Source="library/postgres:16-alpine"; Target="postgres:16-alpine"},
    @{Source="library/redis:7-alpine"; Target="redis:7-alpine"},
    @{Source="library/eclipse-mosquitto:2"; Target="eclipse-mosquitto:2"},
    @{Source="library/influxdb:2.7"; Target="influxdb:2.7"},
    @{Source="grafana/grafana:10.2.3"; Target="grafana/grafana:10.2.3"}
)

$successCount = 0
$failCount = 0

foreach ($image in $images) {
    $sourceImage = "$MIRROR/$($image.Source)"
    $targetImage = $image.Target
    
    Write-Host "正在拉取: $targetImage" -ForegroundColor Yellow
    Write-Host "  镜像源: $sourceImage" -ForegroundColor Gray
    
    try {
        # 拉取镜像
        docker pull $sourceImage 2>&1 | Out-Null
        
        if ($LASTEXITCODE -eq 0) {
            # 重新标记为原始镜像名
            docker tag $sourceImage $targetImage 2>&1 | Out-Null
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "  ✓ 成功: $targetImage" -ForegroundColor Green
                $successCount++
            } else {
                Write-Host "  ✗ 标记失败: $targetImage" -ForegroundColor Red
                $failCount++
            }
        } else {
            Write-Host "  ✗ 拉取失败: $targetImage" -ForegroundColor Red
            $failCount++
        }
    } catch {
        Write-Host "  ✗ 错误: $_" -ForegroundColor Red
        $failCount++
    }
    
    Write-Host ""
}

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "拉取完成" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "成功: $successCount" -ForegroundColor Green
Write-Host "失败: $failCount" -ForegroundColor Red
Write-Host ""

if ($failCount -gt 0) {
    Write-Host "部分镜像拉取失败，建议：" -ForegroundColor Yellow
    Write-Host "1. 检查网络连接" -ForegroundColor Yellow
    Write-Host "2. 配置 Docker 镜像加速器（参考 docker-mirror-setup.md）" -ForegroundColor Yellow
    Write-Host "3. 尝试使用 VPN 或代理" -ForegroundColor Yellow
    Write-Host ""
    exit 1
} else {
    Write-Host "所有镜像已就绪，可以开始构建！" -ForegroundColor Green
    Write-Host ""
    Write-Host "执行以下命令开始部署：" -ForegroundColor Cyan
    Write-Host "  docker compose up -d --build" -ForegroundColor White
    Write-Host ""
    
    $response = Read-Host "是否现在开始构建和部署？(y/n)"
    if ($response -eq 'y' -or $response -eq 'Y') {
        Write-Host ""
        Write-Host "开始构建和部署..." -ForegroundColor Yellow
        docker compose up -d --build
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "部署成功！" -ForegroundColor Green
            Write-Host ""
            Write-Host "服务访问地址：" -ForegroundColor Cyan
            Write-Host "  miniEMS:  http://localhost:8080" -ForegroundColor Green
            Write-Host "  Grafana:  http://localhost:3000" -ForegroundColor Green
            Write-Host "  InfluxDB: http://localhost:8086" -ForegroundColor Green
            Write-Host ""
            
            docker compose ps
        } else {
            Write-Host ""
            Write-Host "部署失败，请查看错误信息" -ForegroundColor Red
            exit 1
        }
    }
}
