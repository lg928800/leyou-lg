package com.leyou.page.service;

import com.leyou.common.exceptions.LyException;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpuDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/10 17:08
 * @description:
 */
@Service
public class PageService {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private SpringTemplateEngine engine;

    /**
     * 查询item数据
     * @param id
     * @return
     */
    public Map<String, Object> loadDataById(Long id) {
        // 1.查询商品数据
        SpuDTO spuDTO = itemClient.querySpuById(id);

        // 2.通过spu中的id集合查询分类数据
        List<CategoryDTO> categoryDTOS = itemClient.queryCategoryByIds(spuDTO.getCategoryIds());

        // 3.同理查询品牌数据
        BrandDTO brandDTO = itemClient.queryById(spuDTO.getBrandId());

        // 4.查询规格组及参数
        List<SpecGroupDTO> specGroupDTOS = itemClient.querySpecsByCid(spuDTO.getCid3());

        // 5.封装到map结合中
        Map<String, Object> map = new HashMap<>();
        map.put("categories", categoryDTOS);
        map.put("brand", brandDTO);
        map.put("spuName", spuDTO.getName());
        map.put("subTitle", spuDTO.getSubTitle());
        map.put("skus", spuDTO.getSkus());
        map.put("detail", spuDTO.getSpuDetail());
        map.put("specs", specGroupDTOS);
        return map;
    }
    // 本地保存静态页面路径
    private static final String HTML_DIR = "D:\\javaTool\\nginx-1.12.2\\html\\item";

    /**
     * 保存静态页面
     * @param id
     */
    public void createItemHtml(Long id) {
        // 1.创建上下文
        Context context = new Context();
        // 2.将查询的数据加载到上下文中
        context.setVariables(loadDataById(id));
        // 3.创建本地文件的路径
        File file = new File(HTML_DIR, id + ".html");

        // 3.1文件输出到本地
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {

            engine.process("item", context, writer);
        } catch (Exception e) {
            throw new LyException(500,e);
        }
    }

}
