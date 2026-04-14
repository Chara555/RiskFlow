package org.example.config;

import org.example.core.service.GeoIpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

/**
 * GeoIP 配置类
 *
 * <p>启动时加载 classpath:geoip/ 下的 .mmdb 离线数据库文件，
 * 文件缺失时对应能力自动降级，不影响系统启动。
 */
@Configuration
public class GeoIpConfig {

    private static final Logger log = LoggerFactory.getLogger(GeoIpConfig.class);

    @Value("${riskflow.geoip.country-db-path:classpath:geoip/GeoLite2-Country.mmdb}")
    private String countryDbPath;

    @Value("${riskflow.geoip.asn-db-path:classpath:geoip/GeoLite2-ASN.mmdb}")
    private String asnDbPath;

    @Bean
    public GeoIpService geoIpService() {
        GeoIpService service = new GeoIpService();

        // 加载 Country 库
        File countryFile = resolveMmdbFile(countryDbPath, "GeoLite2-Country");
        service.initCountryDatabase(countryFile);

        // 加载 ASN 库（可选，缺失时降级）
        File asnFile = resolveMmdbFile(asnDbPath, "GeoLite2-ASN");
        service.initAsnDatabase(asnFile);

        log.info("[GeoIpConfig] GeoIpService 初始化完成. Country库={}, ASN库={}",
                service.isCountryAvailable(), service.isAsnAvailable());

        return service;
    }

    /**
     * 解析 mmdb 文件路径
     *
     * <p>支持两种格式：
     * <ul>
     *   <li>classpath: 前缀 → 从 classpath 解析为临时文件（MaxMind 需要随机访问）</li>
     *   <li>绝对路径 → 直接使用</li>
     * </ul>
     */
    private File resolveMmdbFile(String path, String dbName) {
        if (path == null || path.isBlank()) {
            return null;
        }

        // classpath: 前缀处理
        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length());
            try {
                ClassPathResource resource = new ClassPathResource(resourcePath);
                if (resource.exists()) {
                    // MaxMind DatabaseReader 需要随机访问文件，从 classpath 提取到临时文件
                    File tempFile = File.createTempFile(dbName, ".mmdb");
                    tempFile.deleteOnExit();
                    try (var is = resource.getInputStream();
                         var os = new java.io.FileOutputStream(tempFile)) {
                        is.transferTo(os);
                    }
                    log.info("[GeoIpConfig] 从 classpath 提取 {} 到临时文件: {}", dbName, tempFile.getAbsolutePath());
                    return tempFile;
                } else {
                    log.warn("[GeoIpConfig] {} 在 classpath 中未找到: {}", dbName, resourcePath);
                    return null;
                }
            } catch (IOException e) {
                log.warn("[GeoIpConfig] 从 classpath 提取 {} 失败: {}", dbName, resourcePath, e);
                return null;
            }
        }

        // 绝对路径
        return new File(path);
    }
}
