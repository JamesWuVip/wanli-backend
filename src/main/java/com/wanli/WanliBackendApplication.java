package com.wanli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 万里后端应用程序主类.
     *
 * @author JamesWu.
 * @since 1.0.0.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class WanliBackendApplication {



    /**
     * 应用程序入口点.
     *
     * @param args 命令行参数.
     */
    public static void main(final String[] args) {
        SpringApplication.run(WanliBackendApplication.class, args);
    }

}