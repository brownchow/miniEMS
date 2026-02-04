# Modbus 功能说明

## 概述
本项目已成功将 Modbus4j 替换为 j2mod，并实现了完整的 Modbus 通信功能，包括主站、从站和模拟器。

## 主要功能

### 1. Modbus 主站服务 (ModbusMasterService)
- 支持 TCP 和 RTU 模式
- 可读取和写入保持寄存器、输入寄存器、线圈
- 电池数据读取和设置
- 电池充电/放电模拟
- 周期性数据读取

### 2. Modbus 从站服务 (ModbusSlaveService)
- 模拟电池设备行为
- 响应主站的读取请求
- 支持保持寄存器、输入寄存器、线圈
- 电池参数更新
- 电池放电模拟

### 3. Modbus 模拟器服务 (ModbusSimulatorService)
- 独立于 Modbus 通信的电池模拟器
- 模拟电池充电、放电、空闲状态
- 提供电池数据供其他服务使用
- 支持参数配置

## API 接口

### 电池数据相关
- `GET /api/modbus/battery` - 获取电池数据
- `POST /api/modbus/battery` - 设置电池数据

### Modbus 从站操作
- `GET /api/modbus/slave` - 读取从站寄存器
- `POST /api/modbus/slave` - 写入从站寄存器

### 模拟器控制
- `GET /api/modbus/simulate` - 获取模拟器数据
- `POST /api/modbus/simulate` - 设置模拟器参数
- `POST /api/modbus/simulate/start` - 启动模拟器
- `POST /api/modbus/simulate/stop` - 停止模拟器

### 状态查询
- `GET /api/modbus/status` - 获取 Modbus 状态

## 配置说明

### application-modbus.yaml
```yaml
modbus:
  mode: tcp          # 通信模式：tcp 或 rtu
  tcp:
    host: localhost  # TCP 主机
    port: 502        # TCP 端口
  rtu:
    port: /dev/ttyUSB0  # RTU 串口
    baudrate: 9600      # RTU 波特率

simulator:
  enabled: true      # 是否启用模拟器
  update-interval: 5000  # 更新间隔（毫秒）
  battery-count: 16      # 电池数量
```

### Docker Compose 配置
在 `compose.yaml` 中已添加相关环境变量：
- `MODBUS_MODE`: 通信模式 (tcp/rtu)
- `MODBUS_TCP_HOST`: TCP 主机
- `MODBUS_TCP_PORT`: TCP 端口
- `MODBUS_PORT`: Modbus 端口

## 使用方法

### 1. 启动服务
```bash
docker compose up -d
```

### 2. 测试 Modbus 通信
```bash
# 获取电池数据
curl http://localhost:8080/api/modbus/battery

# 设置电池数据
curl -X POST http://localhost:8080/api/modbus/battery \
  -H "Content-Type: application/json" \
  -d '{"soc": 80.0, "voltage": 52.5, "current": 20.0, "temperature": 25.0}'

# 获取模拟器数据
curl http://localhost:8080/api/modbus/simulate
```

### 3. 测试 Modbus 从站
```bash
# 读取从站寄存器
curl "http://localhost:8080/api/modbus/slave?slaveId=1&startAddress=0&quantity=10"

# 写入从站寄存器
curl -X POST "http://localhost:8080/api/modbus/slave?slaveId=1&startAddress=0" \
  -H "Content-Type: application/json" \
  -d '[3500,3500,3500,3500,3500,3500,3500,3500,3500,3500]'
```

## 测试

### 单元测试
```bash
mvn test -Dtest=ModbusServiceTest
```

### 集成测试
```bash
# 启动所有服务
docker compose up -d

# 运行集成测试
mvn test -Dtest=IntegrationTest
```

## 注意事项

1. **端口冲突**: 确保 Modbus 端口 (默认 502) 没有被其他服务占用
2. **网络配置**: Modbus 从站和主站需要在同一网络中
3. **权限**: RTU 模式需要相应的串口权限
4. **防火墙**: 如果需要外部访问，请配置防火墙规则

## 故障排查

### 常见问题

1. **连接超时**
   - 检查 Modbus 从站是否已启动
   - 验证网络连接和端口配置
   - 检查防火墙设置

2. **数据读取失败**
   - 确认从站地址配置正确
   - 检查寄存器地址范围
   - 验证从站功能码支持

3. **模拟器不工作**
   - 检查模拟器服务是否已启动
   - 验证配置参数
   - 查看日志输出

### 日志查看
```bash
docker compose logs miniems-backend
docker compose logs miniems-modbus-simulator
```

## 性能优化

1. **连接池**: 可为 Modbus 主站添加连接池
2. **异步处理**: 使用异步方式处理 Modbus 请求
3. **缓存**: 缓存频繁读取的数据
4. **批量操作**: 批量读取/写入寄存器以减少网络开销

## 扩展

1. **支持更多功能码**: 可添加对其他 Modbus 功能码的支持
2. **安全认证**: 添加 Modbus 通信安全认证
3. **监控告警**: 添加 Modbus 通信状态监控
4. **数据持久化**: 将 Modbus 数据持久化到数据库