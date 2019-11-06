package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {

    int insertCategoryBrand(@Param("ids") List<Long> ids,@Param("bid") Long bid);
    @Delete("DELETE FROM tb_category_brand WHERE brand_id =#{bid}")
    void deleteCategoryBrand(@Param("bid") Long bid);
    @Delete("DELETE FROM tb_brand WHERE id=#{id}")
    void deleteBrandById(Long id);
    @Select("SELECT tb.id, tb.name, tb.letter, tb.image " +
            "FROM tb_category_brand tcb INNER JOIN tb_brand tb ON tb.id = tcb.brand_id WHERE tcb.category_id =#{cid}")
    List<Brand> queryBrandBycategoryId(@Param("cid") Long cid);
}
