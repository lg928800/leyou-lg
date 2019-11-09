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
import com.netflix.config.AggregatedConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilterBuilder;
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

    /**
     * spu 转换goods
     *
     * @param spu
     * @return
     */
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
        List<Map<String, Object>> skus = new ArrayList<>();
        //5.创建price价格的集合
        TreeSet<Long> price = new TreeSet<>();
        //5.1.遍历sku集合
        for (SkuDTO sku : skuList) {
            //创建list中的泛型map集合
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("image", StringUtils.substringBefore(sku.getImages(), ","));
            map.put("price", sku.getPrice());
            map.put("title", sku.getTitle());
            //将map集合添加到list中
            skus.add(map);
            //封装价格信息
            price.add(sku.getPrice());
        }
        //6.这里定义规格参数的集合，因为是Key和Value的结构所以我们定义map集合
        Map<String, Object> specs = new HashMap<>();
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
            specs.put(Key, value);
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

    /**
     * 尺寸区间
     *
     * @param value
     * @param p
     * @return
     */
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
     *
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
        searchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        // 2.1搜索条件
        QueryBuilder queryBuilder = buildBasicQuery(request);
        searchQueryBuilder.withQuery(queryBuilder);
        // 2.2分页查询
        int page = request.getPage() - 1;
        int size = request.getSize();
        searchQueryBuilder.withPageable(PageRequest.of(page, size));
        // 2.3排序功能
        String sortBy = request.getSortBy();
        if (StringUtils.isNoneEmpty(sortBy)) {
            SortOrder order = request.getDesc() ? SortOrder.DESC : SortOrder.ASC;
            searchQueryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(order));
        }
        // 3.搜索结果?????一下都不是很懂
        AggregatedPage<Goods> result = template.queryForPage(searchQueryBuilder.build(), Goods.class);
        // 4.解析结果
        long total = result.getTotalElements();
        int totalPages = result.getTotalPages();
        List<Goods> list = result.getContent();
        // 4.1转换数据类型
        List<GoodsDTO> goodsDTOS = BeanHelper.copyWithCollection(list, GoodsDTO.class);
        return new PageResult<>(total, totalPages, goodsDTOS);


    }

    /**
     * 单独定义查询条件
     *
     * @param request
     * @return
     */
    private QueryBuilder buildBasicQuery(SearchRequest request) {
        // 1.创建bool查询的query
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 1.1一般创建出查询条件后，会向queryBuilder中天健查询条件,这里第一个查询条件是must查询
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()));
        // 1.2filter过滤条件
        Map<String, String> filters = request.getFilter();
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            // 过滤条件的名称name key
            String key = entry.getKey();
            if ("分类".equals(key)) {
                key = "categoryId";
            } else if ("品牌".equals(key)) {
                key = "brandId";
            } else {
                key = "specs."+key;
            }
            queryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }

        return queryBuilder;
    }

    /**
     * 过滤查询
     *
     * @param request
     * @return
     */
    public Map<String, List<?>> queryFilters(SearchRequest request) {
        // 1.定义map过滤的集合
        Map<String, List<?>> filterList = new LinkedHashMap<>();
        // 2.创建原生搜索器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 2.1获取查询条件
        QueryBuilder basicQuery = buildBasicQuery(request);
        queryBuilder.withQuery(basicQuery);
        // 2.2分页查询
        queryBuilder.withPageable(PageRequest.of(0, 1));
        // 2.3显示空的source????这是什么意思
        queryBuilder.withSourceFilter(new FetchSourceFilterBuilder().build());

        // 3.定义聚合条件
        // 3.1分类聚合
        String categoryAgg = "categoryAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAgg).field("categoryId"));
        // 3.2品牌聚合
        String brandAgg = "brandAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAgg).field("brandId"));
        // 4.解析结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        // 4.1获取聚合结果
        Aggregations aggregations = result.getAggregations();
        // 4.2获取分类的聚合
        LongTerms cTerms = aggregations.get(categoryAgg);
        List<Long> idList = handleCategoryAgg(cTerms, filterList);
        LongTerms bTerms = aggregations.get(brandAgg);
        handleBrandAgg(bTerms, filterList);
        if (idList != null && idList.size() == 1) {
            handleSpecAgg(idList.get(0), basicQuery, filterList);
        }
        return filterList;
    }

    /**
     * 规格参数聚合添加map
     *
     * @param cid
     * @param basicQuery
     * @param filterList
     */
    private void handleSpecAgg(Long cid, QueryBuilder basicQuery, Map<String, List<?>> filterList) {
        // 1.通过cid查询规格参数的信息,这里的searching查询可参与搜索的
        List<SpecParamDTO> params = itemClient.querySpecParams(null, cid, true);
        // 2.创建原生查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 2.1搜索条件
        queryBuilder.withQuery(basicQuery);
        // 2.2每页显示结果
        queryBuilder.withPageable(PageRequest.of(0, 1));
        // 2.3显示空的source?/?
        queryBuilder.withSourceFilter(new FetchSourceFilterBuilder().build());
        // 3.遍历查询规格参数的结果，进行聚合
        for (SpecParamDTO param : params) {
            String name = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name));
        }
        // 3.1获取聚合结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = result.getAggregations();
        // 4.重新遍历规格参数的集合
        for (SpecParamDTO param : params) {
            String name = param.getName();
            StringTerms terms = aggregations.get(name);
            // 获取其中的数据值，value
            List<String> paramValues = terms.getBuckets().stream()
                    .map(StringTerms.Bucket::getKeyAsString)
                    .collect(Collectors.toList());
            filterList.put(name,paramValues);
        }
    }

    /**
     * 品牌聚合-->查询添加到map
     *
     * @param bTerms
     * @param filterList
     */
    private void handleBrandAgg(LongTerms bTerms, Map<String, List<?>> filterList) {
        List<Long> bidList = bTerms.getBuckets().stream()
                .map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());
        List<BrandDTO> brandDTOS = itemClient.queryByIds(bidList);
        filterList.put("品牌", brandDTOS);
    }

    /**
     * 分类聚合-->查询添加到map
     *
     * @param terms
     * @param filterList
     */
    private List<Long> handleCategoryAgg(LongTerms terms, Map<String, List<?>> filterList) {
        // 通过聚合对象，来获取buckets,并获取其中的brandids来查询
        List<Long> idlist = terms.getBuckets().stream()
                .map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());
        List<CategoryDTO> categoryDTOS = itemClient.queryCategoryByIds(idlist);
        filterList.put("分类", categoryDTOS);
        return idlist;
    }
}
