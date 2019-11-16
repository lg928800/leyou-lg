package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.RsaUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.UserDTO;
import com.netflix.client.http.HttpResponse;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/14 17:43
 * @description:
 */
@Service
public class AuthService {
    @Autowired
    private JwtProperties prop;
    @Autowired
    private UserClient userClient;

    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * 用户名授权,登录
     * @param username 用户名
     * @param password 密码
     * @param response 通过response来写入cookie的值
     */
    public void login(String username, String password, HttpServletResponse response) {
        // 1.查询用户是否存在，远程调用client方法
        try {
            UserDTO user = userClient.queryUserByUsernameAndPassword(username, password);
            // 2.判断是否查询到
            if (user == null) {
                // 抛出异常
                throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
            }
            // 3.通过秘钥生成token
            String token = JwtUtils.generateTokenExpireInMinutes(
                    new UserInfo(user.getId(), user.getUsername(), "Guest"),
                    prop.getPrivateKey(), prop.getUser().getExpire()
            );

            // 4.写入cookie
            CookieUtils.newCookieBuilder()
                    .response(response)
                    .name(prop.getUser().getCookieName())
                    .value(token)
                    .httpOnly(true)
                    .domain(prop.getUser().getDomain())
                    .build();
        } catch (LyException e) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }


    }

    /**
     * 页面回显登录信息
     * @param request 接收cookie
     * @param response 响应cookie
     * @return userInfo
     */
    public UserInfo verifyUser(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1.获取cookie的value
            String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
            // 2.通过cookie的值，调用工具类获取用户的用户名信息
            Payload<UserInfo> payLoad = JwtUtils.getInfoFromToken(token, RsaUtils.getPublicKey(prop.getPubKeyPath()),UserInfo.class);
            // 2.1获取userinfo中的id,先判断redis中是否存在
            String id = payLoad.getId();
            Boolean b = redisTemplate.hasKey(id);
            if (b != null && b) {
                throw new LyException(ExceptionEnum.UNAUTHORIZED);
            }
            // 3.获取过期时间，用来对比cookie是否过期
            Date expiration = payLoad.getExpiration();
            // 4.获取刷新的时间 ？？？？
            DateTime refreshTime = new DateTime(expiration.getTime()).minusMinutes(prop.getUser().getMinRefreshInterval());
            // 5.判断是否过了当前的刷新时间
            if (refreshTime.isBefore(System.currentTimeMillis())) {
                // 重新生成心的token
                token = JwtUtils.
                        generateTokenExpireInMinutes(payLoad.getUserInfo(), prop.getPrivateKey(), prop.getUser().getExpire());
                // 生成cookie
                CookieUtils.newCookieBuilder()
                        .response(response)
                        .name(prop.getUser().getCookieName())
                        .value(token)
                        .httpOnly(true)
                        .domain(prop.getUser().getDomain())
                        .build();
            }
            return payLoad.getUserInfo();
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }

    }

    /**
     * 登出
     * @param request
     * @param response
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 获取token
        String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
        // 解析token的值,通过jwt公钥解密
        Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);
        // 获取id属性，和过期时间
        String id = payload.getId();
        Date expiration = payload.getExpiration();
        // 获取还剩的存活时间
        long time = expiration.getTime() - System.currentTimeMillis();
        if (time > 5000) {
            // 判断是否大于5秒,存入redis中
            redisTemplate.opsForValue().set(id,"",time, TimeUnit.MILLISECONDS);
        }
        // 否则删除cookie
        CookieUtils.deleteCookie(
                prop.getUser().getCookieName(),
                prop.getUser().getDomain(),
                response);

    }
}
