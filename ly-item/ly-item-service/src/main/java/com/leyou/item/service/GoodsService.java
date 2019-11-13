package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import com.leyou.item.entity.SPU;
import com.leyou.item.entity.Sku;
import com.leyou.item.entity.SpuDetail;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.stream.Collectors;

import static com.leyou.common.constants.MQConstants.Exchange.ITEM_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_DOWN_KEY;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_UP_KEY;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/5 10:47
 * @description:
 */
@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SkuMapper skuMapper;

    /**
     * 商品分页查询
     *
     * @param page
     * @param rows
     * @param saleable
     * @param key
     * @return
     */
    public PageResult<SpuDTO> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        //1.工具类查询实现分页
        PageHelper.startPage(page, rows);
        //2.创建过滤条件
        Example example = new Example(SPU.class);
        Example.Criteria criteria = example.createCriteria();
        //2.1.判断key是否不为空
        if (StringUtils.isNoneEmpty(key)) {
            criteria.orLike("name", "%" + key + "%");
        }
        //2.2判断是否上下过滤
        if (saleable != null) {
            criteria.orEqualTo("saleable", saleable);
        }
        //3.将排序的规则封装到过滤器中
        example.setOrderByClause("update_time DESC");
        //4.查询过滤的SPU数据
        List<SPU> list = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //5.解析分页数据
        PageInfo<SPU> info = new PageInfo<>(list);
        //6.将spu转换成spudto类型数据
        List<SpuDTO> spuDTOS = BeanHelper.copyWithCollection(list, SpuDTO.class);
        //7.据说是调用方法出去分类和品牌查询
        handleCategoryAndBrandName(spuDTOS);
        //8.返回数据
        return new PageResult<>(info.getTotal(),info.getPages(), spuDTOS);
    }

    /**
     * 工具方法调用
     *
     * @param spuDTOList
     */
    public void handleCategoryAndBrandName(List<SpuDTO> spuDTOList) {
        //遍历spudtolist
        for (SpuDTO spu : spuDTOList) {
            //查询
            String categoryName = categoryService.queryCategoryByIds(spu.getCategoryIds()).stream().map(CategoryDTO::getName).
                    collect(Collectors.joining("/"));
            spu.setCategoryName(categoryName);
            BrandDTO brandDTO = brandService.queryById(spu.getBrandId());
            spu.setBrandName(brandDTO.getName());
        }
    }

    /**
     * 新增商品信息
     *
     * @param spuDTO
     */
    @Transactional
    public void saveGoods(SpuDTO spuDTO) {
        //1.转换参数类型
        SPU spu = BeanHelper.copyProperties(spuDTO, SPU.class);
        //1.1.spu对象中有上下架的说明，这里默认下架，因为是新添加的商品
        spu.setSaleable(false);
        //2.添加spu的商品信息
        int count = spuMapper.insertSelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //3.获取spu_detail数据
        SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
        //4.转换数据类型
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDetailDTO, SpuDetail.class);
        //5.获取spu_id从spu的数据中
        spuDetail.setSpuId(spu.getId());
        //6.新增数据
        count = spuDetailMapper.insertSelective(spuDetail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        saveSku(spuDTO);
        /*List<Sku> skuList1 = skuList.stream()
                .map(sku -> {
                    sku.setSpuId(spu.getId());
                    sku.setEnable(false);
                    return sku;
                }).collect(Collectors.toList());*/

    }

    private void saveSku(SpuDTO spuDTO) {
        int count;//7.通过spudto中获取skus的数据
        List<SkuDTO> skus = spuDTO.getSkus();
        //8.转换数据类型
        List<Sku> skuList = BeanHelper.copyWithCollection(skus, Sku.class);
        //9.遍历集合数据设置id和上下架属性
        for (Sku sku : skuList) {
            sku.setSpuId(spuDTO.getId());
            sku.setEnable(false);
        }
        //10.调用通用mapper中的新增集合的方法
        count = skuMapper.insertList(skuList);
        if (count != skuList.size()) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }
    @Autowired
    private AmqpTemplate amqpTemplate;
    /**
     * 修改商品上架或者下架
     * @param id
     * @param saleable
     */
    @Transactional
    public void updateSpuSaleable(Long id, Boolean saleable) {
        //1.因为spu和sku中都有商品的上下架的属性所以分部修改，这里先修改spu中的属性
        SPU spu = new SPU();
        spu.setId(id);
        spu.setSaleable(saleable);
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //2.修改sku表中的上下架数据,这里我们使用mapper工具中的example对象来封装数据id
        Sku sku = new Sku();
        sku.setEnable(saleable);
        Example example = new Example(Sku.class);
        example.createCriteria().orEqualTo("spuId",id);
        //2.1调用mapper来修改
        count = skuMapper.updateByExampleSelective(sku, example);
        //2.2判断是否查到数据的话需要用到size长度，我们不知道有多少条需要修改，所以这里可以通过id查询一下有多少条修改的数据
        int size = skuMapper.selectCountByExample(example);
        if (count != size) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        // 3.新增发送mq消息队列
        String key = saleable ? ITEM_UP_KEY : ITEM_DOWN_KEY;
        amqpTemplate.convertAndSend(ITEM_EXCHANGE_NAME,key,spu.getId());
    }

    /**
     * 通过id查询spu的商品数据
     * @param id
     * @return
     */
    public SpuDTO querySpuById(Long id) {
        //1.先查询spu中的数据
        SPU spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //2.转换数据类型，以便后面封装数据
        SpuDTO spuDTO = BeanHelper.copyProperties(spu, SpuDTO.class);
        //3.通过id查询spudetail的数据，可以定义一个方法来实现
        SpuDetailDTO spuDetail = querySpuDetailBySpuId(id);
        //4.将detail数据封装到spudto中
        spuDTO.setSpuDetail(spuDetail);
        //5.查询sku的数据
        List<SkuDTO> list = querySkuBySpuId(id);
        spuDTO.setSkus(list);
        //2.返回数据，别忘记转换数据类型
        return spuDTO;
    }

    public List<SkuDTO> querySkuBySpuId(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> list = skuMapper.select(sku);
        if (list == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list,SkuDTO.class);
    }

    public SpuDetailDTO querySpuDetailBySpuId(Long id) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);
        //转换类型
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyProperties(spuDetail,SpuDetailDTO.class);
    }
    @Transactional
    /**
     * 修改商品数据
     */
    public void updateGoods(SpuDTO spuDTO) {
        //1.这里我们可以直接修改spu表中的数据
        SPU spu = BeanHelper.copyProperties(spuDTO, SPU.class);
        spu.setSaleable(null);
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //2.修改detail表中的数据
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), SpuDetail.class);
        count = spuDetailMapper.updateByPrimaryKeySelective(spuDetail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //3.因为我们不清楚用户是否有增加或者删除sku表中的数据，所以这里我们先删除原来的数据在添加进去
        Sku sku = new Sku();
        sku.setSpuId(spuDTO.getId());
        //3.1通过spuid删除sku中的数据,因为不知道要删除多少数据所以我们先查询出来
        int size = skuMapper.selectCount(sku);
        int delete = skuMapper.delete(sku);
        if (size > 0) {
            if (delete < size) {
                throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
            }
        }
        //3.2.调取之前封装的方法新增sku
        saveSku(spuDTO);



    }
}
