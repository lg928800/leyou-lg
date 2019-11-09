package com.leyou.search.web;

import com.leyou.common.vo.PageResult;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/8 21:37
 * @description:
 */
@RestController
public class SearchController {
    @Autowired
    private SearchService searchService;

    /**
     * 搜索功能
     * @param request
     * @return
     */
    @PostMapping("/page")
    public ResponseEntity<PageResult<GoodsDTO>> search(@RequestBody SearchRequest request) {
        //直接返回结果
        return ResponseEntity.ok(searchService.search(request));

    }

    @PostMapping("/filter")
    public ResponseEntity<Map<String, List<?>>> queryFilters(@RequestBody SearchRequest request) {
        // 直接返回结果
        return ResponseEntity.ok(searchService.queryFilters(request));
    }
}
