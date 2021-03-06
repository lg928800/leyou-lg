package com.leyou.user.entity;

import com.leyou.common.constants.RegexPatterns;
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Table(name = "tb_user")
@Data
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    @Pattern(regexp = RegexPatterns.USERNAME_REGEX,message = "用户名格式不正确！")
    private String username;
    @Pattern(regexp = RegexPatterns.USERNAME_REGEX,message = "密码格式不正确！")
    private String password;
    @Pattern(regexp = RegexPatterns.PHONE_REGEX,message = "手机格式不正确!")
    private String phone;
    private Date createTime;
    private Date updateTime;
}