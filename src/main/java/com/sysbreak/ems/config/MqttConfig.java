package com.sysbreak.ems.config;

import com.sysbreak.ems.mqtt.MqttMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;

/**
 * MQTT 配置类
 * <p>
 * 【这个类是做什么的？】
 *
 * @author sysbreak
 * @Configuration 注解标记这个类是一个配置类，Spring 会读取其中的 @Bean 方法来创建 Bean。
 * <p>
 * 【简单消息流程】
 * MQTT Broker
 * │
 * ▼
 * MqttPahoMessageDrivenChannelAdapter  (收到消息，重写 messageArrived 直接处理)
 * │
 * ▼
 * MqttMessageHandler.handleMessage()  (业务逻辑处理)
 * <p>
 * 【核心问题解决】
 * Spring Integration MQTT 要求必须有 outputChannel。
 * 但重写 messageArrived 后，消息不会真正通过通道发送。
 * 设置一个 dummy 通道满足框架要求，同时用重写的 messageArrived 直接处理消息。
 * @since 2026-01-16
 */
@Slf4j
@Configuration
public class MqttConfig {

    /**
     * MQTT Broker 地址，默认 tcp://localhost:1883
     */
    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    /**
     * MQTT 客户端ID，默认 miniEMS-随机值
     */
    @Value("${mqtt.client.id}")
    private String clientId;

    /**
     * 要订阅的 Topic，默认 ems/bms/telemetry
     */
    @Value("${mqtt.topic}")
    private String topic;

    /**
     * 创建一个"虚拟"通道
     * <p>
     * 【为什么需要这个通道？】
     * MqttPahoMessageDrivenChannelAdapter 继承自 MessageProducerSupport，
     * 在 afterSingletonsInstantiated() 时会检查是否有 outputChannel。
     * 即使重写了 messageArrived，框架仍然要求配置 outputChannel。
     * 这里创建一个不会被真正使用的通道来满足框架要求。
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        log.info("[MQTT配置] 创建虚拟输入通道（满足框架要求）");
        return new DirectChannel();
    }

    /**
     * 创建 MQTT 客户端工厂
     * <p>
     * 【MqttPahoClientFactory 的作用】
     * 负责创建和管理 MQTT 客户端连接，类似于数据库连接池。
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        log.info("[MQTT配置] 创建MqttClientFactory，Broker地址: {}", brokerUrl);

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setKeepAliveInterval(30);
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        factory.setConnectionOptions(options);

        return factory;
    }

    /**
     * 创建 MQTT 接收适配器
     * <p>
     * 【核心做法】
     * 1. 设置 outputChannel 满足框架要求（虽然消息不走这里）
     * 2. 重写 messageArrived() 直接处理消息，不经过通道
     * <p>
     * 【配置参数说明】
     * - clientId: 客户端标识
     * - factory: 连接工厂
     * - topics: 要订阅的 Topic 数组
     * - setCompletionTimeout: 连接超时时间
     * - setQos: 消息质量等级（1=至少一次）
     * - setOutputChannel: 设置通道（满足框架要求）
     */
    @Bean
    public MessageProducer mqttInbound(MqttMessageHandler handler) {
        log.info("[MQTT配置] 创建MQTT接收适配器");
        log.info("[MQTT配置]   - ClientID: {}", clientId);
        log.info("[MQTT配置]   - 订阅Topic: {}", topic);

        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        clientId,
                        mqttClientFactory(),
                        topic.split(",")
                ) {
                    /**
                     * 重写消息到达方法，直接处理消息
                     *
                     * 【为什么重写？】
                     * 父类实现会将消息发送到 outputChannel。
                     * 但实际业务中，我们需要直接处理消息，不经过通道。
                     * 重写此方法，消息到达后直接调用业务处理器。
                     *
                     * @param topic 消息Topic
                     * @param message MQTT消息对象
                     */
                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        String payload = new String(message.getPayload());
                        log.info("");
                        log.info("╔═══════════════════════════════════════════════════════════════");
                        log.info("║ [MQTT消息] 收到消息");
                        log.info("╠═══════════════════════════════════════════════════════════════");
                        log.info("║ Topic: {}", topic);
                        log.info("║ Payload: {}", payload);
                        log.info("╚═══════════════════════════════════════════════════════════════");
                        log.info("");

                        try {
                            handler.handleMessage(payload, topic);
                        } catch (Exception e) {
                            log.error("[MQTT消息] 处理消息异常: {}", e.getMessage(), e);
                        }
                    }
                };

        adapter.setCompletionTimeout(5000);
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());

        return adapter;
    }
}
