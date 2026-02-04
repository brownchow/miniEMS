package com.sysbreak.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 启动类
 *
 * @author sysbreak
 * @since 2016-01-14
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class EmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmsApplication.class, args);
    }

}
