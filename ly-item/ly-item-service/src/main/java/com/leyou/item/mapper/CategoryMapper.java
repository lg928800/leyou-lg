package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.entity.Category;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/10/31 20:06
 * @description:
 */
public interface CategoryMapper extends BaseMapper<Category> {
    @Select("SELECT tc.id, tc.`name`, tc.parent_id, tc.is_parent, tc.sort \n" +
            "FROM tb_category tc INNER JOIN tb_category_brand tcb ON tc.id = tcb.category_id WHERE tcb.brand_id = #{id}")
    List<Category> queryByBrandId(Long brandId);

}
