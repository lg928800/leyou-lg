package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.Category;
import com.leyou.item.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/10/31 20:05
 * @description:
 */
@Service
public class CategoryService {
    //注入mapper
    @Autowired
    private CategoryMapper categoryMapper;

    public List<CategoryDTO> queryListByParent(Long pid) {
        //创建Category对象将Pid的数据封装进去
        Category category = new Category();
        category.setParentId(pid);
        List<Category> list = categoryMapper.select(category);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list,CategoryDTO.class);
    }

    public List<CategoryDTO> queryListByBrandId(Long brandId) {
        //1.通过id查询分类的数据
        List<Category> list = categoryMapper.queryByBrandId(brandId);
        //2.判断list集合是否为空,如果不存在则抛出异常信息
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        //3.如果不为空则将category对象的数据转换成categoryDTO类型的数据
        return BeanHelper.copyWithCollection(list, CategoryDTO.class);

    }

    /**
     * 根据ids拓展查询分类信息
     * @param ids
     * @return
     */
    public List<CategoryDTO> queryCategoryByIds(List<Long> ids) {
        //1.根据ids查询分类数据
        List<Category> list = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list,CategoryDTO.class);

    }
}
