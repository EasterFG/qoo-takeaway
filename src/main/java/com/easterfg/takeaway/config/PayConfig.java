package com.easterfg.takeaway.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

/**
 * @author EasterFG on 2022/10/14
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class PayConfig {

    private String appId;

    private String serverUrl;

    private String notify;

    private String publicKey;

    private String charset;

    private String signType;

    private String privateKey;

    /**
     * 支付宝付款接口配置
     */
    @Bean
    public AlipayConfig alipayConfig() throws IOException {
        AlipayConfig config = new AlipayConfig();
        config.setServerUrl(serverUrl);
        config.setAppId(appId);
        config.setSignType(signType);
        config.setCharset(charset);
        config.setFormat("json");
        config.setAlipayPublicKey(publicKey);
        // 为避免私钥随源码泄露，推荐从文件中读取私钥字符串而不是写入源码中
        config.setPrivateKey(FileUtils.readFileToString(new File(privateKey)));
        return config;
    }

    @Bean
    @SneakyThrows
    public AlipayClient alipayClient(AlipayConfig alipayConfig) {
        return new DefaultAlipayClient(alipayConfig);
    }
}
