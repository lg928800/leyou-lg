package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entity.SPU;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/5 10:47
 * @description:
 */
@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    public PageResult<SpuDTO> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        //1.工具类查询实现分页
        PageHelper.startPage(page,rows);
        //2.创建过滤条件
        Example example = new Example(SPU.class);
        Example.Criteria criteria = example.createCriteria();
        //2.1.判断key是否不为空
        if (StringUtils.isNoneEmpty(key)) {
            criteria.orLike("name","%"+key+"%");
        }
        //2.2判断是否上下过滤
        if (saleable != null) {
            criteria.orEqualTo("saleable",saleable);
        }
        //3.将排序的规则封装到过滤器中
        example.setOrderByClause("update_time DESC");
        //4.查询过滤的SPU数据
        List<SPU> list = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //5.解析分页数据
        PageInfo<SPU> info = new PageInfo<>(list);
        //6.将spu转换成spudto类型数据
        List<SpuDTO> spuDTOS = BeanHelper.copyWithCollection(list, SpuDTO.class);
        //7.据说是调用方法出去分类和品牌查询
        handleCategoryAndBrandName(spuDTOS);
        //8.返回数据
        return new PageResult<>(info.getTotal(),spuDTOS);
    }

    public void handleCategoryAndBrandName(List<SpuDTO> spuDTOList) {
        //遍历spudtolist
        for (SpuDTO spu : spuDTOList) {
            //查询
            String categoryName = categoryService.queryCategoryByIds(spu.getCategoryIds()).stream().map(CategoryDTO::getName).
                    collect(Collectors.joining("/"));
            spu.setCategoryName(categoryName);
            BrandDTO brandDTO = brandService.queryById(spu.getBrandId());
            spu.setBrandName(brandDTO.getName());
        }
    }
}
