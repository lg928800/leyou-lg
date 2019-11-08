package com.leyou.search.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.*;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.pojo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/8 10:15
 * @description:
 */
@Service
public class SearchService {
    //注入itemClient
    @Autowired
    private ItemClient itemClient;

    public Goods buildGoods(SpuDTO spu) {
        //1 这里我们直接先获取spu的id方便后面使用
        Long spuId = spu.getId();
        //1.1.先获取分类，品牌的标题
        String categoryNames = spu.getCategoryName();
        //1.2.判断分类名称是否为空，如果为空需要通过spuid查询
        if (StringUtils.isBlank(categoryNames)) {
            categoryNames = itemClient.queryCategoryByIds(spu.getCategoryIds())
                    .stream()
                    .map(CategoryDTO::getName)
                    .collect(Collectors.joining());
        }
        //2.通过spu来查询品牌标题
        String brandName = spu.getBrandName();
        if (StringUtils.isBlank(brandName)) {
            brandName = itemClient.queryById(spuId).getName();
        }
        //3.拼接name的属性，与goods中的all对应
        String all = spu.getName() + categoryNames + brandName;
        //4.定义规格参数的属性
        List<SkuDTO> skuList = spu.getSkus();
        //4.1.判断是否为空，来进行查询，添加数据
        if (CollectionUtils.isEmpty(skuList)) {
            skuList = itemClient.querySkuBySpuId(spuId);
        }
        //4.2.创建map集合来封装sku的属性
        List<Map<String,Object>> skus = new ArrayList<>();
        //5.创建price价格的集合
        TreeSet<Long> price = new TreeSet<>();
        //5.1.遍历sku集合
        for (SkuDTO sku : skuList) {
            //创建list中的泛型map集合
            Map<String, Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("image", StringUtils.substringBefore(sku.getImages(), ","));
            map.put("price", sku.getPrice());
            map.put("title", sku.getTitle());
            //将map集合添加到list中
            skus.add(map);
            //封装价格信息
            price.add(sku.getPrice());
        }
        //6.这里定义规格参数的集合，因为是Key和Value的结构所以我们定义map集合
        Map<String,Object> specs = new HashMap<>();
        //6.1调用方法查询params
        List<SpecParamDTO> params = itemClient.querySpecParams(null, spu.getCid3(), true);
        //7.查询detail
        SpuDetailDTO detail = spu.getSpuDetail();
        if (detail == null) {
            detail = itemClient.queryDetailBySpuId(spuId);
        }
        //7.1获取通用的参数
        String json = detail.getGenericSpec();
        Map<Long, Object> genericSpec = JsonUtils.toMap(json, Long.class, Object.class);
        //7.2获取特有的参数
        json = detail.getSpecialSpec();
        Map<Long, Object> specialSpec = JsonUtils.toMap(json, Long.class, Object.class);
        //8.遍历之前查询的规格参数集合,用来封装数据
        for (SpecParamDTO param : params) {
            //分别定义key和value的属性
            String Key = param.getName();
            Object value = null;
            //这里判断参数中是否是通用属性
            if (param.getGeneric()) {
                value = genericSpec.get(param.getId());
            } else {
                value = specialSpec.get(param.getId());
            }
            //判断是否为数值类型，后面单独调用一个方法，这里并不理解所以把调用大方法cv了
            if (param.getNumeric()) {
                value = chooseSegment(value, param);
            }
            //添加到specs的map中
            specs.put(Key,value);
        }
        //9.创建转换的对象
        Goods goods = new Goods();
        goods.setId(spuId);
        goods.setSubTitle(spu.getSubTitle());
        goods.setSkus(JsonUtils.toString(skus));
        goods.setAll(all);
        goods.setBrandId(spu.getBrandId());
        goods.setCategoryId(spu.getCid3());
        goods.setPrice(price);
        goods.setSpecs(specs);
        goods.setCreateTime(spu.getCreateTime().getTime());
        return goods;


    }

    private String chooseSegment(Object value, SpecParamDTO p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }
    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }
    @Autowired
    private ElasticsearchTemplate template;
    /**
     * 索引库分页查询
     * @param request
     * @return
     */
    public PageResult<GoodsDTO> search(SearchRequest request) {
        // 0.健壮性判断
        String key = request.getKey();
        if (StringUtils.isBlank(key)) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        //1.首先先创建原生搜索器
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        // 2.过滤source条件
        searchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"},null));
        // 2.1搜索条件
        searchQueryBuilder.withQuery(QueryBuilders.matchQuery("all",key));
        // 2.2分页查询
        int page = request.getPage()-1;
        int size = request.getSize();
        searchQueryBuilder.withPageable(PageRequest.of(page, size));
        // 3.搜索结果?????一下都不是很懂
        AggregatedPage<Goods> result = template.queryForPage(searchQueryBuilder.build(), Goods.class);
        // 4.解析结果
        long total = result.getTotalElements();
        int totalPages = result.getTotalPages();
        List<Goods> list = result.getContent();
        // 4.1转换数据类型
        List<GoodsDTO> goodsDTOS = BeanHelper.copyWithCollection(list, GoodsDTO.class);
        return new PageResult<>(total,totalPages,goodsDTOS);


    }
}
