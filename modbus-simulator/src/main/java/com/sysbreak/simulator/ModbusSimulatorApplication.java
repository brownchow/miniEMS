package com.sysbreak.simulator;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Modbus Battery Simulator Application
 * Simulates BMS battery data and publishes to MQTT broker
 *
 * @author sysbreak
 * @since 2026-01-16
 */
@Slf4j
public class ModbusSimulatorApplication {

    private static final String MQTT_BROKER = System.getenv().getOrDefault("MQTT_BROKER", "tcp://mqtt:1883");
    private static final String MQTT_TOPIC = System.getenv().getOrDefault("MQTT_TOPIC", "ems/bms/telemetry");
    private static final int PUBLISH_INTERVAL = Integer.parseInt(System.getenv().getOrDefault("PUBLISH_INTERVAL", "5000"));
    private static final String CLIENT_ID = "modbus-simulator-" + System.currentTimeMillis();

    private final Random random = new Random();
    private MqttClient mqttClient;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Battery simulation parameters
    private double soc = 85.0; // State of Charge (%)
    private double voltage = 52.5; // Battery voltage (V)
    private double current = 15.0; // Battery current (A)
    private double temperature = 25.0; // Battery temperature (°C)

    public static void main(String[] args) {
        ModbusSimulatorApplication app = new ModbusSimulatorApplication();
        app.start();
    }

    public void start() {
        log.info("=== Modbus Battery Simulator Starting ===");
        log.info("MQTT Broker: {}", MQTT_BROKER);
        log.info("MQTT Topic: {}", MQTT_TOPIC);
        log.info("Publish Interval: {} ms", PUBLISH_INTERVAL);

        // Initialize MQTT connection
        connectMqtt();

        // Start periodic data publishing
        scheduler.scheduleAtFixedRate(this::publishBatteryData, 0, PUBLISH_INTERVAL, TimeUnit.MILLISECONDS);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        log.info("Simulator started successfully");
    }

    private void connectMqtt() {
        try {
            mqttClient = new MqttClient(MQTT_BROKER, CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setKeepAliveInterval(30);
            options.setConnectionTimeout(10);

            mqttClient.connect(options);
            log.info("连接 MQTT broker: {}", MQTT_BROKER);
        } catch (MqttException e) {
            log.error("连接 MQTT broker 失败", e);
            throw new RuntimeException("MQTT Broker 连接失败", e);
        }
    }

    private void publishBatteryData() {
        try {
            // Simulate battery data changes
            updateBatteryParameters();

            // Create telemetry data
            Map<String, Object> telemetry = createTelemetryData();

            // Convert to JSON
            String jsonPayload = JSON.toJSONString(telemetry);

            // Publish to MQTT
            MqttMessage message = new MqttMessage(jsonPayload.getBytes());
            message.setQos(1);
            message.setRetained(false);

            mqttClient.publish(MQTT_TOPIC, message);
            log.info("Published battery data: SOC={}%, Voltage={}V, Current={}A, Temp={}°C",
                    String.format("%.2f", soc),
                    String.format("%.2f", voltage),
                    String.format("%.2f", current),
                    String.format("%.2f", temperature));

        } catch (Exception e) {
            log.error("Failed to publish battery data", e);
        }
    }

    private void updateBatteryParameters() {
        // Simulate realistic battery behavior
        // SOC slowly decreases (discharging)
        soc = Math.max(10.0, soc - random.nextDouble() * 0.5);

        // Voltage fluctuates slightly (typical for Li-ion: 48V-58V)
        voltage = 48.0 + (soc / 100.0) * 10.0 + (random.nextDouble() - 0.5) * 2.0;

        // Current fluctuates (0-50A)
        current = 10.0 + random.nextDouble() * 40.0;

        // Temperature varies (20-35°C)
        temperature = 20.0 + random.nextDouble() * 15.0;
    }

    private Map<String, Object> createTelemetryData() {
        Map<String, Object> data = new HashMap<>();

        data.put("deviceId", "BMS-001");
        data.put("timestamp", System.currentTimeMillis());
        data.put("soc", Math.round(soc * 100.0) / 100.0);
        data.put("voltage", Math.round(voltage * 100.0) / 100.0);
        data.put("current", Math.round(current * 100.0) / 100.0);
        data.put("temperature", Math.round(temperature * 100.0) / 100.0);
        data.put("power", Math.round(voltage * current * 100.0) / 100.0);

        // Battery cell voltages (simulate 16 cells)
        double[] cellVoltages = new double[16];
        for (int i = 0; i < 16; i++) {
            cellVoltages[i] = Math.round((voltage / 16.0 + (random.nextDouble() - 0.5) * 0.2) * 1000.0) / 1000.0;
        }
        data.put("cellVoltages", cellVoltages);

        // Battery status
        data.put("status", soc > 20 ? "NORMAL" : "LOW_SOC");
        data.put("chargingStatus", current > 0 ? "DISCHARGING" : "CHARGING");

        return data;
    }

    private void shutdown() {
        log.info("Shutting down simulator...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (Exception e) {
            log.error("Error during shutdown", e);
        }
        log.info("Simulator stopped");
    }
}
