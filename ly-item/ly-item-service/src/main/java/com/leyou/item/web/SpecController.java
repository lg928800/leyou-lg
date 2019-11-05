package com.leyou.item.web;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 添加规格组数据，因为是添加需求所以不需要返回值
     * @return
     */
    @PostMapping("/group")
    public ResponseEntity<Void> addSpecGroup(@RequestBody SpecGroupDTO specGroupDTO) {
        //1.调用service方法
        specService.addSpecGroup(specGroupDTO);
        //2.返回添加成功的响应
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    /**
     * 修改规格组的信息
     * @return
     */
    @PutMapping("/group")
    public ResponseEntity<Void> editSpecGroup(@RequestBody SpecGroupDTO specGroupDTO) {
        //1.修改和添加的流程差不多，调取service方法,这里没有返回值
        specService.editSpecGroup(specGroupDTO);
        //2.响应修改成功给前端
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据id删除规格组的信息
     * @param id
     * @return
     */
    @DeleteMapping("/group/{id}")
    public ResponseEntity<Void> deleteSpecGroupById(@PathVariable("id") Long id) {
        //1.直接调用service来删除，没有返回值,增删改基本都没有返回值
        specService.deleteSpecGroupById(id);
        //2.返回删除成功的响应信息
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 新增规格参数信息
     * @param specParamDTO
     * @return
     */
    @PostMapping("/param")
    public ResponseEntity<Void> addSpecParam(@RequestBody SpecParamDTO specParamDTO) {
        //1.增删改仿照规格组那一套，这里不做过多解释
        specService.addSpecParam(specParamDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改规格参数信息
     * @return
     */
    @PutMapping("/param")
    public ResponseEntity<Void> editSpecParam(@RequestBody SpecParamDTO specParamDTO) {
        specService.editSpecParam(specParamDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 通过id删除规格参数的数据
     * @param id
     * @return
     */
    @DeleteMapping("/param/{id}")
    public ResponseEntity<Void> deleteSpecParamById(@PathVariable("id")Long id) {
        specService.deleteSpecParamById(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }
}
