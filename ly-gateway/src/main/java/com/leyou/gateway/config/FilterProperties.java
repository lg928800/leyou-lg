package com.leyou.gateway.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/16 20:24
 * @description:
 */
@Data
@ConfigurationProperties(prefix = "ly.filter")
public class FilterProperties implements InitializingBean {

    private List<String>  allowPaths;

    // 定义map集合封装allowPaths数据
    private Map<String,String> allowPathAndMethod =new HashMap<>();

    /**
     * 初始化后，处理map数据
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 遍历集合
        for (String allowPath : allowPaths) {
            if (!allowPath.contains(":")) {
                // 判断allowPath中是否包含:
                throw new RuntimeException("白名单路径不符合规范！");
            }
            // 使用切割来处理allow中的数据
            String[] arr = allowPath.split(":");
            allowPathAndMethod.put(arr[1], arr[0]);

        }
    }
}
