package com.wanli.config;

import io.sentry.Sentry;
import io.sentry.SentryOptions;
import io.sentry.spring.boot.SentryProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;

/**
 * Sentry错误追踪配置类.
 * 用于配置错误监控和性能追踪.
     *
 * @author wanli-backend.
 * @since 1.0.0.
 */
@Configuration
@ConditionalOnProperty(name = "sentry.dsn")
public class SentryConfig {

    /**
     * 生产环境采样率.
     */
    private static final double PRODUCTION_SAMPLE_RATE = 0.05;

    /**
     * 预发布环境采样率.
     */
    private static final double STAGING_SAMPLE_RATE = 0.1;

    /**
     * 开发环境采样率.
     */
    private static final double DEVELOPMENT_SAMPLE_RATE = 1.0;

    /** Sentry DSN配置. */
    @Value("${sentry.dsn:}")
    private String sentryDsn;

    /** 环境配置. */
    @Value("${sentry.environment:development}")
    private String environment;

    /** 链路追踪采样率. */
    @Value("${sentry.traces-sample-rate:0.1}")
    private Double tracesSampleRate;

    /** 性能分析采样率. */
    @Value("${sentry.profiles-sample-rate:0.1}")
    private Double profilesSampleRate;

    /** 调试模式开关. */
    @Value("${sentry.debug:false}")
    private Boolean debug;

    /** Spring环境对象. */
    private final Environment env;

    /**
     * 构造函数.
     *
     * @param springEnv Spring环境对象.
     */
    public SentryConfig(final Environment springEnv) {
        this.env = springEnv;
    }

    /**
     * 初始化Sentry配置.
     */
    @PostConstruct
    public void init() {
        if (sentryDsn != null && !sentryDsn.isEmpty()) {
            Sentry.init(options -> {
                options.setDsn(sentryDsn);
                options.setEnvironment(environment);
                options.setTracesSampleRate(tracesSampleRate);
                options.setProfilesSampleRate(profilesSampleRate);
                options.setDebug(debug);

                // 设置发布版本
                String version = getClass().getPackage()
                        .getImplementationVersion();
                if (version != null) {
                    options.setRelease(version);
                }

                // 配置标签
                options.setTag("application", "wanli-backend");
                options.setTag("profile",
                        String.join(",",
                                env.getActiveProfiles()));

                // 配置上下文
                options.setServerName(getServerName());

                // 配置采样
                configureSampling(options);

                // 配置过滤器
                configureFilters(options);
            });
        }
    }

    /**
     * 配置采样策略.
     *
     * @param options Sentry选项.
     */
    private void configureSampling(final SentryOptions options) {
        // 根据环境调整采样率
        if ("production".equals(environment)) {
            options.setTracesSampleRate(PRODUCTION_SAMPLE_RATE);
            options.setProfilesSampleRate(PRODUCTION_SAMPLE_RATE);
        } else if ("staging".equals(environment)) {
            options.setTracesSampleRate(STAGING_SAMPLE_RATE);
            options.setProfilesSampleRate(STAGING_SAMPLE_RATE);
        } else {
            options.setTracesSampleRate(DEVELOPMENT_SAMPLE_RATE);
            options.setProfilesSampleRate(
                    DEVELOPMENT_SAMPLE_RATE);
        }
    }

    /**
     * 配置过滤器.
     *
     * @param options Sentry选项.
     */
    private void configureFilters(final SentryOptions options) {
        // 过滤健康检查请求
        options.setBeforeSend((event, hint) -> {
            if (event.getRequest() != null
                    && event.getRequest().getUrl() != null) {
                String url = event.getRequest().getUrl();
                if (url.contains("/actuator/health")
                        || url.contains("/health")
                        || url.contains("/metrics")) {
                    return null; // 不发送健康检查相关的事件
                }
            }
            return event;
        });

        // 过滤敏感信息
        options.setBeforeSendTransaction((transaction, hint) -> {
            // 可以在这里过滤敏感的事务信息
            return transaction;
        });
    }

    /**
     * 获取服务器名称.
     *
     * @return 服务器名称.
     */
    private String getServerName() {
        String serverName = System.getenv("HOSTNAME");
        if (serverName == null || serverName.isEmpty()) {
            serverName = System.getenv("COMPUTERNAME");
        }
        if (serverName == null || serverName.isEmpty()) {
            serverName = "unknown";
        }
        return serverName;
    }

    /**
     * 自定义Sentry属性配置.
     *
     * @return Sentry属性配置.
     */
    @Bean
    @ConditionalOnProperty(name = "sentry.dsn")
    public SentryProperties sentryProperties() {
        SentryProperties properties = new SentryProperties();
        properties.setDsn(sentryDsn);
        properties.setEnvironment(environment);
        properties.setTracesSampleRate(tracesSampleRate);
        properties.setDebug(debug);
        return properties;
    }
}