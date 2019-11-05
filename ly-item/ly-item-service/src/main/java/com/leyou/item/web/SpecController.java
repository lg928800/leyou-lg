package com.leyou.item.web;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.service.SpecService;
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
 * @date: 2019/11/4 20:08
 * @description:
 */
@RestController
@RequestMapping("spec")
public class SpecController {
    //注入service实例对象
    @Autowired
    private SpecService specService;

    /**
     * 通过id查询规格组信息
     * @param id
     * @return
     */
    @GetMapping("/groups/of/category")
    public ResponseEntity<List<SpecGroupDTO>> queryGroupByCategory(@RequestParam("id") Long id) {
        //直接返回查询的数据即可
        return ResponseEntity.ok(specService.queryGroupByCategory(id));
    }

    /**
     * 通过id查询规格参数的详细信息
     * @param gid
     * @return
     */
    @GetMapping("/params")
    public ResponseEntity<List<SpecParamDTO>> querySpecParams(@RequestParam("gid") Long gid) {
        //直接返回查询结果即可
        return ResponseEntity.ok(specService.querySpecParams(gid));
    }
}
