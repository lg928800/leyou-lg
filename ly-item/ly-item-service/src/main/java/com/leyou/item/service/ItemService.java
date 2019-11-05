package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.item.pojo.Item;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/10/31 14:35
 * @description:
 */
@Service
public class ItemService {

    public Item saveItem(Item item) {
        //判空判处异常
        if (item.getPrice() == null) {
            throw new LyException(ExceptionEnum.PRICE_CAN_NOT_ENPTY);
        }
        if (item.getName() == null) {
            throw new LyException(ExceptionEnum.INVALID_PARAMETER);
        }
        int id = new Random().nextInt(100);
        item.setId(id);
        return item;
    }
}
