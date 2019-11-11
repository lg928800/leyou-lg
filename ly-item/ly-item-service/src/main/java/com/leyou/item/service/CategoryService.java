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

import java.util.Arrays;
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

    /**
     * 通过品牌id查询分类数据
     * @param brandId
     * @return
     */
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

    /**
     * 通过cid3查询二级和一级分类属性
     * @param id 三级分类的id
     * @return 分类数据的集合
     */
    public List<CategoryDTO> queryCategoryByCid3(Long id) {
        // 1.先通过3级分类的id查询到分类数据
        Category c3 = categoryMapper.selectByPrimaryKey(id);
        if (c3 == null) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        // 2.在通过3级分类的数据获得2级分类的id进行查询,以此类推
        Category c2 = categoryMapper.selectByPrimaryKey(c3.getParentId());
        if (c2 == null) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        // 3.1级分类的查询
        Category c1 = categoryMapper.selectByPrimaryKey(c2.getParentId());
        if (c1 == null) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        // 4.将3个id添加到集合中
        List<Category> list = Arrays.asList(c1, c2, c3);
        // 5.转换数据类型
        return BeanHelper.copyWithCollection(list,CategoryDTO.class);


    }
}
