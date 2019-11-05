package com.leyou.item.web;

import com.leyou.item.pojo.Item;
import com.leyou.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/10/31 15:12
 * @description:
 */
@RestController
@RequestMapping("item")
public class ItemController {
    //注入service
    @Autowired
    private ItemService itemService;

    @PostMapping
    public ResponseEntity<Item> saveItem(Item item) {

        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.saveItem(item));
    }
}
