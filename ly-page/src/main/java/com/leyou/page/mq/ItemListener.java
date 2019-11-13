package com.leyou.page.mq;

import com.leyou.common.constants.MQConstants;
import com.leyou.page.service.PageService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/12 19:52
 * @description:
 */
@Component
public class ItemListener {

    @Autowired
    private PageService pageService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.Queue.PAGE_ITEM_UP, durable = "true"),
            exchange = @Exchange(name = MQConstants.Exchange.ITEM_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.ITEM_UP_KEY
    ))
    public void listenInsert(Long spuId) {
        // 这里监听的逻辑基本和添加和删除索引库的逻辑是一样的
        if (spuId != null) {
            pageService.createItemHtml(spuId);
        }
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.Queue.PAGE_ITEM_DOWN, durable = "true"),
            exchange = @Exchange(name = MQConstants.Exchange.ITEM_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.ITEM_DOWN_KEY
    ))
    public void listenDelete(Long spuId) {
        // 这里监听的逻辑基本和添加和删除索引库的逻辑是一样的
        if (spuId != null) {
            pageService.deleteItemHtml(spuId);
        }
    }

}
