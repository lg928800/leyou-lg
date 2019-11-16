package com.leyou.auth.web;

import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.entity.UserInfo;
import com.netflix.client.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/14 17:40
 * @description:
 */
@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户名授权
     * @param username 用户名
     * @param password 密码
     * @param response 通过response来写入cookie的值
     * @return 没有返回值
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestParam("username")String username,
                                      @RequestParam("password")String password,
                                      HttpServletResponse response) {
        // 这里登陆授权，调用service方法
        authService.login(username, password, response);
        // 直接返回状态码
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 页面回显登录信息
     * @param request 接收cookie
     * @param response 响应cookie
     * @return userInfo
     */
    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verifyUser(HttpServletRequest request,HttpServletResponse response) {
        // 调用service方法，获取cookie中的值

        return ResponseEntity.ok(authService.verifyUser(request,response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // 没有返回值，调用service方法
        authService.logout(request,response);
        // 返回状态值即可
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
