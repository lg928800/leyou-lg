package com.leyou.item.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("item-service")
public interface ItemClient {
    /**
     * 根据id查询品牌信息，brand信息
     * @param id  品牌的id
     * @return BrandDTO
     */
    @GetMapping("/brand/{id}")
    BrandDTO queryById(@PathVariable("id")Long id);

    /**
     * 通过id集合查询商品分类信息
     * @param idList 接受参数为分类的所有id
     * @return List<CategoryDTO>
     */
    @GetMapping("/category/list")
    List<CategoryDTO> queryCategoryByIds(@RequestParam("ids") List<Long> idList);
    /**
     * 商品分类查询good
     * @param key 可以理解为搜索的关键字
     * @param saleable  上下架
     * @param page  当前的页码
     * @param rows  每页显示的容量
     * @return PageResult<SpuDTO>
     */
    @GetMapping("/spu/page")
    PageResult<SpuDTO> querySpuByPage(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows);

    /**
     * 这里通过spu的id查询spudto的数据
     * 综合了spudetail和skus数据，统一进行了查询
     * @param id    这里是spu的id
     * @return SpuDTO
     */
    @GetMapping("/spu/detail/{id}")
    SpuDTO querySpuById(@PathVariable("id") Long id);

    /**
     * 通过spuid查询skus
     * @param id 这里是spu的id
     * @return List<SkuDTO>
     */
    @GetMapping("/sku/of/spu")
    List<SkuDTO> querySkuBySpuId(@RequestParam("id") Long id);

    /**
     * 通过spuID查询spuDetailDto
     * @param id spu的id
     * @return SpuDetailDTO
     */
    @GetMapping("/detail/{id}")
    SpuDetailDTO queryDetailBySpuId(@PathVariable("id")Long id);

    /**
     * 通过规格组合规格参数的id来查询规格信息
     * @param gid  组id
     * @param cid  规格参数id
     * @param searching  用于搜索用的
     * @return List<SpecParamDTO>
     */
    @GetMapping("/spec/params")
    List<SpecParamDTO> querySpecParams(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching);

    /**
     * 根据id集合查询品牌信息
     * @param ids
     * @return
     */
    @GetMapping("/brand/list")
    List<BrandDTO> queryByIds(@RequestParam("ids") List<Long> ids);
    /**
     * 通过id查询规格组及其参数的数据信息
     * @param id 商品分类的id
     * @return 规格组数据集合里面包含了规格参数的数据集合
     */
    @GetMapping("/spec/of/category")
    List<SpecGroupDTO> querySpecsByCid(@RequestParam("id") Long id);
}
