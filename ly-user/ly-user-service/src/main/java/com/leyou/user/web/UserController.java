package com.leyou.user.web;

import com.leyou.common.exceptions.LyException;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/13 20:52
 * @description:
 */
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 校验数据是否可用
     * @param data 要校验的数据
     * @param type 要校验的数据类型：1,用户名 2，手机号
     * @return 布尔
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUserData(
            @PathVariable("data") String data,
            @PathVariable("type") Integer type) {
        // 调用service方法传入参数，返回结果
        return ResponseEntity.ok(userService.checkData(data,type));
    }

    /**
     * 接受手机号，生成验证码
     * @param phone 手机号
     * @return 无返回值
     */
    @PostMapping("/code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone) {
        //因为没有返回值这里直接返回响应就可以
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 用户注册
     * @param user 封装了用户名，密码，手机号
     * @param code 验证码
     * @return 没有返回值
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid User user, BindingResult result, @RequestParam("code") String code) {

        // 这里健壮性校验，用户名，密码和手机号
        if (result.hasErrors()) {
            // 判断结果是否存在异常，可以手动获取结果集
            // 处理异常结果
            String msg = result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("|"));
            // 判处异常信息
            throw new LyException(400, msg + ",别瞎注册！");
        }
        // 因为没有返回值这里直接返回响应状态即可
        userService.register(user,code);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据用户名密码查询指定的用户
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    @GetMapping("/query")
    public ResponseEntity<UserDTO> queryUserByUsernameAndPassword(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        // 感觉这里的参数可以直接用对象来接受呢
        // 调用service中的方法查询该数据并返回
        return ResponseEntity.ok(userService.queryUserByUsernameAndPassword(username,password));
    }
}
