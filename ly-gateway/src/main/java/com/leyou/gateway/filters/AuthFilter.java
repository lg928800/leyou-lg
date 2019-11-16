package com.leyou.gateway.filters;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/16 19:38
 * @description: 1
 */
@Component
@Slf4j
@EnableConfigurationProperties({JwtProperties.class,FilterProperties.class})
public class AuthFilter extends ZuulFilter {
    @Autowired
    private JwtProperties prop;
    @Autowired
    private FilterProperties filterProp;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 前置通知，返回过滤的类型
     * @return 过滤的类型
     */
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    /**
     * 在哪个阶段执行
     * @return int
     */
    @Override
    public int filterOrder() {
        return FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER + 1;
    }

    /**
     *
     * @return 定义，true拦截器生效执行run方法，反之则不执行
     */
    @Override
    public boolean shouldFilter() {
        // 1.获取request
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        // 2.定义方法，将request传入，获取允许被访问的参数
        Boolean isAllow = IsAllowRequest(request);

        return !isAllow;
    }

    private Boolean IsAllowRequest(HttpServletRequest request) {
        // 遍历filter中的数据路径
        for (Map.Entry<String, String> entry : filterProp.getAllowPathAndMethod().entrySet()) {
            String path = entry.getKey();
            String method = entry.getValue();
            String requestURI = request.getRequestURI();
            if (requestURI.startsWith(path) && ("*".equals(method) || request.getMethod().equalsIgnoreCase(method))) {
                return true;
            }

        }
        return false;
    }

    /**
     * 具体过滤的业务逻辑
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        // 1.获取request
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        try {
            // 2.获取cookie的值，token
            String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
            // 3.解析token
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);
            // 4.判断是否存在黑名单
            String id = payload.getId();
            if (BooleanUtils.isTrue(redisTemplate.hasKey(id))) {
                // 黑名单存在，抛出异常
                throw new RuntimeException("无效的cookie!");
            }
            // TODO: 服务鉴权，用户信息的查询响应,这里记录一下
            UserInfo user = payload.getUserInfo();
            String role = user.getRole(); //角色
            String username = user.getUsername(); //用户名
            String path = request.getRequestURI(); //访问的资源

            log.info("用户{},角色{}，正在访问{}资源！",username,role,path);
        } catch (RuntimeException e) {
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(403);
        }

        return null;
    }
}
