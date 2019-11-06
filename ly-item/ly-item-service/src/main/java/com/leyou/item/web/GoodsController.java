package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/5 10:46
 * @description:
 */
@RestController
public class GoodsController {


    @Autowired
    private GoodsService goodsService;

    /**
     * 商品分类查询good
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuDTO>> querySpuByPage(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows) {
            return ResponseEntity.ok(goodsService.querySpuByPage(page,rows,saleable,key));
    }

    /**
     * 添加商品信息
     * @param spuDTO
     * @return
     */
    @PostMapping("/goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuDTO spuDTO) {
        // 1.没有返回值
        goodsService.saveGoods(spuDTO);
        // 2.直接响应给前端添加成功的信息即可
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/spu/saleable")
    public ResponseEntity<Void> updateSpuSaleable(@RequestParam("id") Long id, @RequestParam("saleable") Boolean saleable) {
        // 1.调用service层
        goodsService.updateSpuSaleable(id,saleable);
        // 2.响应给前端数据
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDTO> querySpuById(@PathVariable("id") Long id) {
        // 通过id查询spu来回显商品信息，用来修改
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }

    @PutMapping("/goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuDTO spuDTO) {
        goodsService.updateGoods(spuDTO);
        // 修改数据，直接返回修改成功的响应
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
