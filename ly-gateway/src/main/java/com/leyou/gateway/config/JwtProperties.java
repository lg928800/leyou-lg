package com.leyou.gateway.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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

    private PublicKey publicKey;


    private UserTokenProperties user = new UserTokenProperties();

    @Data
    public class UserTokenProperties {

        // cookie的名称
        private String cookieName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("【网关】加载公钥失败！", e);
            throw new RuntimeException(e);
        }
    }
}