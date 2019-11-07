package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/1 15:08
 * @description:
 */
@RestController
@RequestMapping("brand")
public class BrandController {
    //注入service层对象
    @Autowired
    private BrandService brandService;

    /**
     * 分页查询
     * @param page
     * @param rows
     * @param key
     * @param sortBy
     * @param desc
     * @return
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<BrandDTO>> queryBrandByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "sortBy",required = false)String sortBy,
            @RequestParam(value = "desc",defaultValue = "false")Boolean desc) {
        return ResponseEntity.ok(brandService.queryBrandByPage(page,rows,key,sortBy,desc));
    }

    /**
     * 添加新增数据
     * @param brandDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(BrandDTO brandDTO) {
        //1.调用serivce层方法,保存数据
        brandService.saveBrand(brandDTO);
        //2.返回需要回显的信息
        return ResponseEntity.status(HttpStatus.CREATED).build(); //这个Build是没有返回值的时候用Build
    }

    /**
     * 修改品牌信息
     * @param brandDTO
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> editBrand(BrandDTO brandDTO) {
        brandService.editBrand(brandDTO);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 删除品牌信息
     * @param id
     * @return
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteBrandById(@RequestParam("id")Long id) {
        //接受前端参数id，通过id删除表中的数据
        brandService.deleteBrandById(id);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据分类id查询品牌信息
     * @param categoryId
     * @return
     */
    @GetMapping("/of/category")
    public ResponseEntity<List<BrandDTO>> queryBrandById(@RequestParam("id")Long categoryId) {
        //1.调用service查询数据
        //2.返回数据
        return ResponseEntity.ok(brandService.queryBrandById(categoryId));
    }

    @GetMapping("{id}")
    public ResponseEntity<BrandDTO> queryById(@PathVariable("id")Long id) {
        // 直接返回数据
        return ResponseEntity.ok(brandService.queryById(id));

    }
}
