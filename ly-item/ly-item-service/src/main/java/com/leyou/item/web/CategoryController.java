package com.leyou.item.web;

import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/10/31 20:05
 * @description:
 */
@RestController
@RequestMapping("category")
public class CategoryController {

    //注入serivce层对象
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/of/parent")
    public ResponseEntity<List<CategoryDTO>> queryByParentId(@RequestParam(value = "pid",defaultValue = "0") Long pid ) {
        return ResponseEntity.ok(categoryService.queryListByParent(pid));
    }

    @GetMapping("/of/brand")
    public ResponseEntity<List<CategoryDTO>> queryByBrandId(@RequestParam("id")Long brandId) {
        //1.通过id查询品牌及对应的类型
        return ResponseEntity.ok(categoryService.queryListByBrandId(brandId));

    }

}
