package com.leyou.upload.controller;

import com.leyou.upload.serivce.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/2 19:59
 * @description:
 */
@RestController
public class UploadController {

    @Autowired
    private UploadService uploadService;

    /**
     * 图片上传
     * 这里的返回值设置为string类型，只需要返回一个URL路径就可以
     * 参数的接受使用springmvc封装的MultipartFile来接受file
     * @return
     */
    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestParam("file")MultipartFile file) {
        //如果上传成功直接返回200和URL数据
        return ResponseEntity.ok(uploadService.upload(file));
    }

    /**
     * 图片签名
     * @return
     */
    @GetMapping("signature")
    public ResponseEntity<Map<String, Object>> getSignature() {
        return ResponseEntity.ok(uploadService.getSignature());
    }
}
