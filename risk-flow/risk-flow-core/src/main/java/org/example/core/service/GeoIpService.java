package org.example.core.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.AsnResponse;
import com.maxmind.geoip2.model.CountryResponse;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 本地离线 GeoIP 解析服务（双库：Country + ASN）
 *
 * <p>基于 MaxMind GeoLite2 离线数据库 (.mmdb)，单次查询耗时约 0.1~0.5ms，无需联网。
 *
 * <p>两个数据库各自独立，缺失某个库时对应能力自动降级（不报错，查询返回 empty）：
 * <ul>
 *   <li>Country 库 → IP 归属国家解析</li>
 *   <li>ASN 库 → IP 归属运营商/云厂商解析（识别机房 IP）</li>
 * </ul>
 */
public class GeoIpService {

    private static final Logger log = LoggerFactory.getLogger(GeoIpService.class);

    /**
     * IPv4 合法格式校验正则（点分十进制，每段 0~255）
     * <p>防止恶意传入域名触发 InetAddress.getByName 的 DNS 反查，导致线程阻塞
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");

    /**
     * IPv6 合法格式校验（标准完整/缩写格式）
     */
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$" // 完整格式
            + "|^(([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4})?::(([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4})?$" // 缩写格式
    );

    private volatile DatabaseReader countryReader;
    private volatile DatabaseReader asnReader;

    /**
     * 初始化 Country 数据库阅读器
     *
     * @param mmdbFile GeoLite2-Country.mmdb 文件
     */
    public void initCountryDatabase(File mmdbFile) {
        if (mmdbFile == null || !mmdbFile.exists()) {
            log.warn("[GeoIpService] Country .mmdb 文件未找到，国家级解析已禁用. path={}", mmdbFile);
            return;
        }
        try {
            this.countryReader = new DatabaseReader.Builder(mmdbFile).build();
            log.info("[GeoIpService] Country 数据库加载成功. path={}", mmdbFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("[GeoIpService] Country 数据库加载失败. path={}", mmdbFile.getAbsolutePath(), e);
        }
    }

    /**
     * 初始化 ASN 数据库阅读器
     *
     * @param mmdbFile GeoLite2-ASN.mmdb 文件
     */
    public void initAsnDatabase(File mmdbFile) {
        if (mmdbFile == null || !mmdbFile.exists()) {
            log.warn("[GeoIpService] ASN .mmdb 文件未找到，ASN解析已禁用. path={}", mmdbFile);
            return;
        }
        try {
            this.asnReader = new DatabaseReader.Builder(mmdbFile).build();
            log.info("[GeoIpService] ASN 数据库加载成功. path={}", mmdbFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("[GeoIpService] ASN 数据库加载失败. path={}", mmdbFile.getAbsolutePath(), e);
        }
    }

    /**
     * 校验 IP 字符串是否为合法的 IPv4 或 IPv6 格式
     * <p>必须在 {@link InetAddress#getByName} 之前调用，
     * 防止恶意传入域名触发 DNS 反查导致线程阻塞（DoS 风险）
     */
    private boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        // 先做格式校验，格式合法的 IP 不会触发 DNS 查询
        return IPV4_PATTERN.matcher(ip).matches() || IPV6_PATTERN.matcher(ip).matches();
    }

    /**
     * 解析 IP 地址对应的国家 ISO 代码（如 CN、US、RU）
     *
     * @param ip IP 地址字符串
     * @return 国家 ISO 代码，解析失败或 Country 库未加载时返回 empty
     */
    public Optional<String> resolveCountryCode(String ip) {
        if (!isValidIpAddress(ip)) {
            log.debug("[GeoIpService] IP 格式非法，跳过解析 ip={}", ip);
            return Optional.empty();
        }
        if (countryReader == null) {
            log.debug("[GeoIpService] Country 阅读器未初始化，跳过解析 ip={}", ip);
            return Optional.empty();
        }
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CountryResponse response = countryReader.country(ipAddress);
            return Optional.ofNullable(response.getCountry().getIsoCode());
        } catch (AddressNotFoundException e) {
            log.debug("[GeoIpService] IP 未在 Country 数据库中找到: {}", ip);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("[GeoIpService] Country 解析异常 ip={}", ip, e);
            return Optional.empty();
        }
    }

    /**
     * 解析 IP 地址对应的 ASN 信息（自治系统号 + 运营商名称）
     *
     * @param ip IP 地址字符串
     * @return ASN 信息，解析失败或 ASN 库未加载时返回 empty
     */
    public Optional<AsnInfo> resolveAsn(String ip) {
        if (!isValidIpAddress(ip)) {
            log.debug("[GeoIpService] IP 格式非法，跳过解析 ip={}", ip);
            return Optional.empty();
        }
        if (asnReader == null) {
            log.debug("[GeoIpService] ASN 阅读器未初始化，跳过解析 ip={}", ip);
            return Optional.empty();
        }
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            AsnResponse response = asnReader.asn(ipAddress);
            return Optional.of(new AsnInfo(
                    response.getAutonomousSystemNumber(),
                    response.getAutonomousSystemOrganization()
            ));
        } catch (AddressNotFoundException e) {
            log.debug("[GeoIpService] IP 未在 ASN 数据库中找到: {}", ip);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("[GeoIpService] ASN 解析异常 ip={}", ip, e);
            return Optional.empty();
        }
    }

    /**
     * Spring 容器销毁时关闭 DatabaseReader，释放底层文件句柄
     * <p>防止热更新重载或服务关闭时出现 "Too many open files"
     */
    @PreDestroy
    public void destroy() {
        try {
            if (countryReader != null) {
                countryReader.close();
                log.info("[GeoIpService] Country 数据库阅读器已关闭");
            }
            if (asnReader != null) {
                asnReader.close();
                log.info("[GeoIpService] ASN 数据库阅读器已关闭");
            }
        } catch (IOException e) {
            log.error("[GeoIpService] 关闭 GeoIP 数据库阅读器异常", e);
        }
    }

    /**
     * Country 库是否可用
     */
    public boolean isCountryAvailable() {
        return countryReader != null;
    }

    /**
     * ASN 库是否可用
     */
    public boolean isAsnAvailable() {
        return asnReader != null;
    }

    /**
     * ASN 解析结果封装
     */
    public record AsnInfo(Long asnNumber, String asnOrganization) {
    }
}
