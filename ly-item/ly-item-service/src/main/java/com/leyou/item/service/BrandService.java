package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.Brand;
import com.leyou.item.mapper.BrandMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

import static java.awt.SystemColor.info;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/1 15:06
 * @description:
 */
@Service
public class BrandService {
    //注入dao层对象
    @Autowired
    private BrandMapper brandMapper;

    /**
     * 分页查询
     * @param page
     * @param rows
     * @param key
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<BrandDTO> queryBrandByPage(Integer page, Integer rows, String key, String sortBy, Boolean desc) {
        //分页。传入的参数是当前页码和每页多少条数据
        PageHelper.startPage(page, rows);
        //过滤套件,并不知道这是哪的
        Example example = new Example(Brand.class);
        //判断查询字段是否不为空，也就是Key
        if (StringUtils.isNoneBlank(key)) {
            example.createCriteria().orLike("name", "%" + key + "%").
                    orEqualTo("letter", key.toUpperCase());
        }
        //排序
        if (StringUtils.isNoneBlank(sortBy)) {
            //这里定义一个字符串来封装需要排序的字段和排序的规则
            String orderByClause = sortBy+ (desc ? " DESC " : " ASC ");
            example.setOrderByClause(orderByClause);
        }
        //查询数据
        List<Brand> brands = brandMapper.selectByExample(example);

        //判断查询结果是否为空
        if (CollectionUtils.isEmpty(brands)) {
            //如果为空则抛出异常，统一进行拦截
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //解析分页结果???
        PageInfo<Brand> info = new PageInfo<>(brands);
        //类型转换，将查询道德集合数据转换成brandDTO的对象
        List<BrandDTO> brandDTOS = BeanHelper.copyWithCollection(brands, BrandDTO.class);

        //返回结果
        return new PageResult<>(info.getTotal(),brandDTOS);

    }

    /**
     * 新增数据
     * @param brandDTO
     */
    @Transactional
    public void saveBrand(BrandDTO brandDTO) {
        //1.如果需要添加数据的话，需要把穿过来的brandDTO的数据类型装换成brand的数据类型
        Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);
        //2.前段传来的数据中心会带有ID，添加数据的话，不需要用到ID，自增长所以我们设置为Null
        brand.setId(null);
        //3.调用dao层的方法来添加数据
        int count = brandMapper.insertSelective(brand);
        //4.将添加成功的影响的Count值返回，判断是否添加成功
        if (count!=1) {
            //如果不等于1则说明没有添加成功，抛出异常信息
            throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //5.因为Brand表单是通过中间表来维护的所以需要通过前台穿过来的ID来，新增中间表的信息也就是tb_category_brand
        List<Long> cids = brandDTO.getCids();
        count = brandMapper.insertCategoryBrand(cids,brand.getId());
        //6.判断返回值是否不等于ids数据的长度，则说明添加失败，ids集合中有多少个id,添加了多少个数据，所以返回值应该和集合的长度一样
        if (count != brandDTO.getCids().size()) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

    }

    /**
     * 修改品牌表单数据
     * @param brandDTO
     */
    public void editBrand(BrandDTO brandDTO) {
        //1.首先先把brandDto类型的数据转换成brand类型的数据
        Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);
        //2.调用dao层的方法修改数据
        int count = brandMapper.updateByPrimaryKeySelective(brand);
        //3.判断返回值是否不等于1，则说明修改失败
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //4.如果添加成功的话，可能品牌与分类两者之间的关联表的数据会变化，所以稳妥起见，决定先删除中建表，然后重新添加中间表数据进行关联
        //4.1通过brand_id来删除中间表信息
        brandMapper.deleteCategoryBrand(brand.getId());

        //5.删除中间表的关系后重新建立中间表的关系
        List<Long> cids = brandDTO.getCids();
        count = brandMapper.insertCategoryBrand(cids, brand.getId());
        //5.1判断count是否等于cids数组的长度
        if (count != cids.size()) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

    }

    /**
     * 删除品牌信息数据
     * @param id
     */
    public void deleteBrandById(Long id) {
        //1.通过id先删除品牌表中的数据
        brandMapper.deleteBrandById(id);
        //2.删除brand的表单数据中后，再删除中间表中关联的数据id
        brandMapper.deleteCategoryBrand(id);
    }

    /**
     * 根据id查询品牌信息
     * @param id
     * @return
     */
    public BrandDTO queryById(Long id) {
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyProperties(brand,BrandDTO.class);
    }
}
