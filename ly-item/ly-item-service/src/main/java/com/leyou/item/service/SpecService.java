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
}
