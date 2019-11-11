package com.leyou.page.web;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/10 16:37
 * @description:
 */
@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping("item/{id}.html")
    public String toItemPage(Model model, @PathVariable("id") Long id) {
        // 定义map集合来封装，分类，品牌，商品及规格组和参数的信息
        Map<String,Object> map = pageService.loadDataById(id);

        model.addAllAttributes(map);
        return "item";
    }
}
