package com.sysbreak.ems.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
public class ModbusServiceTest {

    @Autowired
    private ModbusMasterService modbusMasterService;

    @Autowired
    private ModbusSlaveService modbusSlaveService;

    @Autowired
    private ModbusSimulatorService modbusSimulatorService;

    @Test
    public void testModbusCommunication() throws InterruptedException {
        log.info("Starting Modbus communication test...");
        
        // 测试读取电池数据
        int slaveId = 1;
        BatteryData data = modbusMasterService.readBatteryData(slaveId);
        log.info("Initial battery data: {}", data);
        
        // 测试写入电池参数
        BatteryData newData = new BatteryData();
        newData.setSoc(80.0);
        newData.setVoltage(52.5);
        newData.setCurrent(20.0);
        newData.setTemperature(25.0);
        
        modbusMasterService.writeBatteryParameters(slaveId, newData);
        log.info("Wrote new battery parameters");
        
        // 等待数据更新
        TimeUnit.SECONDS.sleep(2);
        
        // 再次读取验证
        BatteryData updatedData = modbusMasterService.readBatteryData(slaveId);
        log.info("Updated battery data: {}", updatedData);
        
        // 测试电池充电模拟
        log.info("Testing battery charging simulation...");
        modbusMasterService.simulateBatteryCharging(slaveId);
        
        // 测试电池放电模拟
        log.info("Testing battery discharging simulation...");
        modbusSlaveService.simulateBatteryDischarging();
        
        // 测试周期性读取
        log.info("Starting periodic read test...");
        modbusMasterService.periodicRead(slaveId, 5);
        
        // 等待测试完成
        TimeUnit.SECONDS.sleep(15);
        
        log.info("Modbus communication test completed");
    }

    @Test
    public void testSimulator() {
        log.info("Starting simulator test...");
        
        // 测试模拟器功能
        modbusSimulatorService.startSimulation();
        
        // 等待模拟一段时间
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Simulator test completed");
    }

    @Test
    public void testModbusSlave() {
        log.info("Starting Modbus slave test...");
        
        // 测试从站功能
        modbusSlaveService.updateBatteryParameters(75.0, 52.0, 18.0, 26.0);
        
        int soc = modbusSlaveService.readHoldingRegister(30);
        log.info("Read SOC from slave: {}%", soc / 100.0);
        
        int[] voltages = modbusSlaveService.readHoldingRegisters(0, 10);
        log.info("Read voltages from slave: {}", voltages);
        
        log.info("Modbus slave test completed");
    }
}