package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.SpecGroup;
import com.leyou.item.entity.SpecParam;
import com.leyou.item.mapper.ParamMapper;
import com.leyou.item.mapper.SpecGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/4 20:11
 * @description:
 */
@Service
public class SpecService {
    //注入mapper对象实例
    @Autowired
    private SpecGroupMapper specMapper;
    @Autowired
    private ParamMapper paramMapper;

    /**
     * 通过id查询规格组数据
     * @param id
     * @return
     */
    public List<SpecGroupDTO> queryGroupByCategory(Long id) {
        //1.因为是通过id查询规格组的信息，所以这里先创建specgroup对象
        SpecGroup specGroup = new SpecGroup();
        //1.1将参数id，封装到对象里
        specGroup.setCid(id);
        //1.2.调取mapper中的方法来查询规格组的信息数据,得到集合
        List<SpecGroup> list = specMapper.select(specGroup);
        //2.判断查询到的数据是否为空，如果为空则判处异常信息
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //3.将查询到的数据集合转换成specgroupdto对象返回
        return BeanHelper.copyWithCollection(list,SpecGroupDTO.class);
    }

    /**
     * 通过id查询规格参数的信息
     * @param gid
     * @return
     */
    public List<SpecParamDTO> querySpecParams(Long gid) {
        //1.查询规格组参数和查询规格组的信息逻辑差不多，将参数id封装到specparam对象中
        SpecParam param = new SpecParam();
        param.setGroupId(gid);
        //2.调用mapper层查询数据
        List<SpecParam> params = paramMapper.select(param);
        //3.判空数据，判处异常
        if (CollectionUtils.isEmpty(params)) {
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        //4.将查询的数据转换成specparam对象返回即可
        return BeanHelper.copyWithCollection(params,SpecParamDTO.class);

    }

    /**
     * 添加规格组信息
     * @param specGroupDTO
     */
    public void addSpecGroup(SpecGroupDTO specGroupDTO) {
        //1.因为是向数据库中添加数据，所以这里我们把dto的对象转换成specgroup类型的对象
        SpecGroup specGroup = BeanHelper.copyProperties(specGroupDTO, SpecGroup.class);
        //2.因为里面封装了cid和name的属性值，所以这里我们直接调取mapper来添加数据
        int count = specMapper.insertSelective(specGroup);
        //3.判断count是否不等于1,也就是没有添加成功
        if (count != 1) {
            //判处异常信息
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //规格组的表中并没有中间表来关联，所以这里不用做添加中间表的操作
    }

    /**
     * 修改规格组的数据
     * @param specGroupDTO
     */
    public void editSpecGroup(SpecGroupDTO specGroupDTO) {
        //1.将dto类型的数据转换成specgroup类型的数据
        SpecGroup specGroup = BeanHelper.copyProperties(specGroupDTO, SpecGroup.class);
        //2.调用mapper层修改数据
        int count = specMapper.updateByPrimaryKeySelective(specGroup);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //3.没有中间表关联所以不做对中间表的操作
    }

    /**
     * 根据id删除规格组的信息
     * @param id
     */
    public void deleteSpecGroupById(Long id) {
        //1.创建specgroup对象
        SpecGroup specGroup = new SpecGroup();
        //2.将id封装到里面，并调取删除的方法
        specGroup.setId(id);
        int count = specMapper.deleteByPrimaryKey(specGroup);
        if (count != 1) {
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
        //3.没有中间表关联所以不做对中间表的操作
    }

    /**
     * 添加规格参数数据
     * @param specParamDTO
     */
    public void addSpecParam(SpecParamDTO specParamDTO) {
        //1.转换数据类型
        SpecParam specParam = BeanHelper.copyProperties(specParamDTO, SpecParam.class);
        //2.调用Mapper层
        int count = paramMapper.insertSelective(specParam);
        //3.判空
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    /**
     * 修改规格参数信息数据
     * @param specParamDTO
     */
    public void editSpecParam(SpecParamDTO specParamDTO) {
        //1.还是转换数据类型
        SpecParam specParam = BeanHelper.copyProperties(specParamDTO, SpecParam.class);
        //2.调用mapper中的方法
        int count = paramMapper.updateByPrimaryKeySelective(specParam);
        //3.判空
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    /**
     * 根据id删除规格参数的信息
     * @param id
     */
    public void deleteSpecParamById(Long id) {
        //1.创建param对象
        SpecParam param = new SpecParam();
        param.setId(id);
        //2.调用mapper
        int i = paramMapper.deleteByPrimaryKey(param);
        if (i !=1) {
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
    }
}
