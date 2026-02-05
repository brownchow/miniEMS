package com.sysbreak.ems.config;

import com.sysbreak.ems.mqtt.MqttMessageHandler;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;

/**
 * MQTT 终极配置类
 * 放弃所有自动配置，手动物理连接适配器与处理器
 *
 * @author sysbreak
 * @since 2026-01-16
 */
@Configuration
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Value("${mqtt.topic}")
    private String topic;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
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
     * 定义一个绝对路径的管道，强制不使用任何前缀
     */
    @Bean(name = "emsInboundChannel")
    public MessageChannel emsInboundChannel() {
        return new DirectChannel();
    }

    /**
     * 定义适配器，并显式指定发送到上面的管道
     */
    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId, mqttClientFactory(), topic);
        adapter.setCompletionTimeout(5000);
        adapter.setQos(1);
        // 这里必须引用我们定义的 emsInboundChannel Bean
        adapter.setOutputChannel(emsInboundChannel());
        return adapter;
    }
}
