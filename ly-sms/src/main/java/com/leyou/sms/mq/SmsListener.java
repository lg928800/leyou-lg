package com.leyou.sms.mq;

import com.leyou.common.constants.MQConstants;
import com.leyou.common.utils.RegexUtils;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/13 19:45
 * @description:
 */
@Slf4j
@Component
public class SmsListener {


    @Autowired
    private SmsHelper smsHelper;

    @Autowired
    private SmsProperties prop;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.Queue.SMS_VERIFY_CODE_QUEUE, durable = "true"),
            exchange = @Exchange(value = MQConstants.Exchange.SMS_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.VERIFY_CODE_KEY
    ))
    public void listenVerifyCode(Map<String,String> msg) {
        // 1.判断是否为空，则结束方法，放置重复调用
        if (CollectionUtils.isEmpty(msg)) {
            return;
        }
        // 2.调用发送短信的方法
        // 2.1从参数map中获取phone的值
        String phone = msg.get("phone");
        if (!RegexUtils.isPhone(phone)) {
            log.error("发送短信的手机号格式不正确，{}",phone);
            // 手机格式不正确
            return;
        }
        // 2.2获取code验证码，自己拼接
        String code = msg.get("code");
        if (!RegexUtils.isVerifyCode(code)) {
            log.error("发送短信的验证码的格式不正确:{}",code);
            // 验证码不正确
            return;
        }
        String param ="{\"code\":\""+code+"\"}";

        smsHelper.sendMessage(phone,prop.getSignName(),prop.getVerifyCodeTemplate(),param);
    }
}
