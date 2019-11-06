package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.entity.Sku;
import tk.mybatis.mapper.common.special.InsertListMapper;

/**
 * @author lg
 */
public interface SkuMapper extends BaseMapper<Sku>, InsertListMapper<Sku> {
}