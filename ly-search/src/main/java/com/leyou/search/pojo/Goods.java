package com.leyou.search.pojo;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/7 20:15
 * @description:
 */

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;
import java.util.Set;

/**
 * 一个SPU对应一个Goods
 */
@Data
@Document(indexName = "goods", type = "docs", shards = 1, replicas = 1)
public class Goods {
    @Id
    private Long id; // spuId
    private String subTitle;// 卖点
    private String skus;// sku信息的json结构

    private String all; // 所有需要被搜索的信息，包含标题，分类，甚至品牌
    private Long brandId;// 品牌id
    private Long categoryId;// 商品3级分类id
    private Long createTime;// spu创建时间
    private Set<Long> price;// 价格
    private Map<String, Object> specs;// 可搜索的规格参数，key是参数名，值是参数值
}
