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

    /**
     * 通过id查询分类属性
     * @param pid
     * @return
     */
    @GetMapping("/of/parent")
    public ResponseEntity<List<CategoryDTO>> queryByParentId(@RequestParam(value = "pid",defaultValue = "0") Long pid ) {
        return ResponseEntity.ok(categoryService.queryListByParent(pid));
    }

    /**
     * 通过品牌id查询分类属性
     * @param brandId 品牌的id
     * @return 分类属性的集合
     */
    @GetMapping("/of/brand")
    public ResponseEntity<List<CategoryDTO>> queryByBrandId(@RequestParam("id")Long brandId) {
        //1.通过id查询品牌及对应的类型
        return ResponseEntity.ok(categoryService.queryListByBrandId(brandId));

    }

    /**
     * 通过ids集合查询分类
     * @param idList id集合
     * @return 分类集合
     */
    @GetMapping("/list")
    public ResponseEntity<List<CategoryDTO>> queryCategoryByIds(@RequestParam("ids") List<Long> idList) {
        return ResponseEntity.ok(categoryService.queryCategoryByIds(idList));
    }

    /**
     * 通过3级分类id查询前两级分类数据
     * @param id 三级分类id
     * @return 分类集合
     */
    @GetMapping("/levels")
    public ResponseEntity<List<CategoryDTO>> queryCategoryByCid3(@RequestParam("id") Long id) {
        // 返回查询结果即可
        return ResponseEntity.ok(categoryService.queryCategoryByCid3(id));

    }
}
