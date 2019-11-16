package com.leyou.user.service;

import com.leyou.common.constants.MQConstants;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.RegexUtils;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.mapper.UserMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/13 20:51
 * @description:
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 校验数据是否可用
     * @param data 要校验的数据
     * @param type 要校验的数据类型：1,用户名 2，手机号
     * @return 布尔
     */
    public Boolean checkData(String data, Integer type) {
        // 1.这里要校验用户中的用户名或者是手机号，所以这里是通过数据库查询的，创建user对象
        User user = new User();
        // 2.通过type的说明可以判断
        if (type == 1) {
            // 为用户名数据
            user.setUsername(data);
        } else if (type == 2) {
            // 为手机号数据
            user.setPhone(data);
        } else {
            // 在不行就是抛出异常
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        // 3.查询数据
        int count = userMapper.selectCount(user);
        return count == 0;

    }


    // 定义常量参数
    private static final String KEY_PREFIX = "user:send:code:phone:";
    /**
     * 接受手机号，生成验证码
     * @param phone 手机号
     */
    public void sendCode(String phone) {
        // 1.判断手机号是否检验通过
        if (!RegexUtils.isPhone(phone)) {
            throw new LyException(ExceptionEnum.INVALID_PHONE_NUMBER);
        }
        // 2.生成6位随机验证码
        String code = RandomStringUtils.randomNumeric(6);
        // 3.将数据保存到redis中
        redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 2, TimeUnit.MINUTES);
        // 4.发送消息到mq队列中
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);
        amqpTemplate.convertAndSend(MQConstants.Exchange.SMS_EXCHANGE_NAME,
                MQConstants.RoutingKey.VERIFY_CODE_KEY,msg);
    }

    /**
     * 用户注册
     * @param user 封装了用户名，手机号和密码
     * @param code  验证码
     */
    @Transactional
    public void register(User user, String code) {
        // 1.先检验验证码是否正确这里需要从redis中去取，然后对比
        String phone = user.getPhone();
        // 2.检验用户数据
        if (!RegexUtils.isPhone(phone)) {
            throw new LyException(ExceptionEnum.INVALID_PHONE_NUMBER);
        }
        if (!RegexUtils.isVerifyCode(code)) {
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        String cacheCode = redisTemplate.opsForValue().get(KEY_PREFIX + phone);
        if (!StringUtils.equals(code, cacheCode)) {
            // 抛出异常
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        // 3.验证码无误后，加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 4.调用mapper方法添加user数据
        int count = userMapper.insertSelective(user);
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

    }

    public UserDTO queryUserByUsernameAndPassword(String username, String password) {
        // 1.创建user对象，将用户名密码封装进去
        if (username == null && password ==null) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        User u = new User();
        u.setUsername(username);
        // 2.查询
        User user = userMapper.selectOne(u);
        if (user == null) {
            throw new LyException(ExceptionEnum.RESOURCE_NOT_FOUND);
        }
        // 3.检验用户名的密码是否正确
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        return BeanHelper.copyProperties(user, UserDTO.class);

    }
}
