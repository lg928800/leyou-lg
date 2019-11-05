package com.leyou.item.mapper;

import com.leyou.item.entity.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {

    int insertCategoryBrand(@Param("ids") List<Long> ids,@Param("bid") Long bid);
    @Delete("DELETE FROM tb_category_brand WHERE brand_id =#{bid}")
    void deleteCategoryBrand(@Param("bid") Long bid);
    @Delete("DELETE FROM tb_brand WHERE id=#{id}")
    void deleteBrandById(Long id);
}
