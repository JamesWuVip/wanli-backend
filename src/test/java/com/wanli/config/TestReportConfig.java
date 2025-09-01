package com.wanli.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 测试报告配置类
 * 用于配置测试报告的生成和聚合
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@TestConfiguration
@Profile({"test", "integration-test"})
public class TestReportConfig {

    private static final String REPORTS_BASE_DIR = "target/test-reports";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * 获取测试报告基础目录
     * 
     * @return 测试报告基础目录路径
     */
    @Bean
    public Path testReportsBaseDir() {
        final Path reportsDir = Paths.get(REPORTS_BASE_DIR);
        final File reportsDirFile = reportsDir.toFile();
        if (!reportsDirFile.exists()) {
            reportsDirFile.mkdirs();
        }
        return reportsDir;
    }

    /**
     * 获取单元测试报告目录
     * 
     * @return 单元测试报告目录路径
     */
    @Bean
    public Path unitTestReportsDir() {
        final Path unitTestDir = testReportsBaseDir().resolve("unit-tests");
        final File unitTestDirFile = unitTestDir.toFile();
        if (!unitTestDirFile.exists()) {
            unitTestDirFile.mkdirs();
        }
        return unitTestDir;
    }

    /**
     * 获取集成测试报告目录
     * 
     * @return 集成测试报告目录路径
     */
    @Bean
    public Path integrationTestReportsDir() {
        final Path integrationTestDir = testReportsBaseDir().resolve("integration-tests");
        final File integrationTestDirFile = integrationTestDir.toFile();
        if (!integrationTestDirFile.exists()) {
            integrationTestDirFile.mkdirs();
        }
        return integrationTestDir;
    }

    /**
     * 获取E2E测试报告目录
     * 
     * @return E2E测试报告目录路径
     */
    @Bean
    public Path e2eTestReportsDir() {
        final Path e2eTestDir = testReportsBaseDir().resolve("e2e-tests");
        final File e2eTestDirFile = e2eTestDir.toFile();
        if (!e2eTestDirFile.exists()) {
            e2eTestDirFile.mkdirs();
        }
        return e2eTestDir;
    }

    /**
     * 获取覆盖率报告目录
     * 
     * @return 覆盖率报告目录路径
     */
    @Bean
    public Path coverageReportsDir() {
        final Path coverageDir = testReportsBaseDir().resolve("coverage");
        final File coverageDirFile = coverageDir.toFile();
        if (!coverageDirFile.exists()) {
            coverageDirFile.mkdirs();
        }
        return coverageDir;
    }

    /**
     * 获取性能测试报告目录
     * 
     * @return 性能测试报告目录路径
     */
    @Bean
    public Path performanceTestReportsDir() {
        final Path performanceDir = testReportsBaseDir().resolve("performance");
        final File performanceDirFile = performanceDir.toFile();
        if (!performanceDirFile.exists()) {
            performanceDirFile.mkdirs();
        }
        return performanceDir;
    }

    /**
     * 获取聚合报告目录
     * 
     * @return 聚合报告目录路径
     */
    @Bean
    public Path aggregatedReportsDir() {
        final String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        final Path aggregatedDir = testReportsBaseDir().resolve("aggregated").resolve(timestamp);
        final File aggregatedDirFile = aggregatedDir.toFile();
        if (!aggregatedDirFile.exists()) {
            aggregatedDirFile.mkdirs();
        }
        return aggregatedDir;
    }

    /**
     * 获取测试数据目录
     * 
     * @return 测试数据目录路径
     */
    @Bean
    public Path testDataDir() {
        final Path testDataDir = Paths.get("src/test/resources/test-data");
        final File testDataDirFile = testDataDir.toFile();
        if (!testDataDirFile.exists()) {
            testDataDirFile.mkdirs();
        }
        return testDataDir;
    }

    /**
     * 获取测试结果归档目录
     * 
     * @return 测试结果归档目录路径
     */
    @Bean
    public Path testResultsArchiveDir() {
        final Path archiveDir = testReportsBaseDir().resolve("archive");
        final File archiveDirFile = archiveDir.toFile();
        if (!archiveDirFile.exists()) {
            archiveDirFile.mkdirs();
        }
        return archiveDir;
    }
}