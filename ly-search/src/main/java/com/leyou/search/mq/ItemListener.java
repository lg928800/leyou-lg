package com.leyou.search.mq;

import com.leyou.common.constants.MQConstants;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.search.service.SearchService;
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
 * @date: 2019/11/12 19:35
 * @description:
 */
@Component // 方法spring容器中加载
public class ItemListener {

    @Autowired
    private SearchService searchService;

    /**
     * 监听上传消息，添加索引库数据
     * @param spuId spuid
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.Queue.SEARCH_ITEM_UP, durable = "true"),
            exchange = @Exchange(name = MQConstants.Exchange.ITEM_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.ITEM_UP_KEY
    ))
    public void listenInsert(Long spuId) {
        // 这里定义上传添加索引库的方法
        if (spuId != null) {
            searchService.createIndex(spuId);
        }

    }

    /**
     * 监听下架消息，删除索引库消息
     * @param spuId spuid
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.Queue.SEARCH_ITEM_DOWN, durable = "true"),
            exchange = @Exchange(name = MQConstants.Exchange.ITEM_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.ITEM_DOWN_KEY
    ))
    public void listenDelete(Long spuId) {
        // 这里定义上传添加索引库的方法
        if (spuId != null) {
            searchService.deleteById(spuId);
        }

    }
}
