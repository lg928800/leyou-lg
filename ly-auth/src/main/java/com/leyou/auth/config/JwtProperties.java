package com.leyou.auth.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {
    /**
     * 公钥路径地址
     */
    private String pubKeyPath;
    /**
     * 私钥路径地址
     */
    private String priKeyPath;

    private PublicKey publicKey;

    private PrivateKey privateKey;

    private UserTokenProperties user = new UserTokenProperties();

    @Data
    public class UserTokenProperties {
        // 过期时间
        private int expire;
        // 一级路径domain
        private String domain;
        // cookie的名称
        private String cookieName;
        // 刷新时间
        private int minRefreshInterval;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥和私钥失败！", e);
            throw new RuntimeException(e);
        }
    }
}