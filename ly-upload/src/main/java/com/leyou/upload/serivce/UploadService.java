package com.leyou.upload.serivce;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.upload.config.OSSProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/2 20:00
 * @description:
 */
@Service
public class UploadService {
    private static final String IMAGE_DIR = "D:\\javaTool\\nginx-1.12.2\\html\\images";
    //图片的rurl路径
    private static final String IMAGE_URL = "http://image.leyou.com/images";
    //这里并不知道是什么
    private static final List<String> ALLOW_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/bmp");

    /**
     * 上传图片
     *
     * @param file
     * @return
     */
    public String upload(MultipartFile file) {
        //1.文件校验
        String contentType = file.getContentType();
        //2.这里判断文件是否属于三种类型的其中一种，如果不是则抛出异常
        if (!ALLOW_IMAGE_TYPES.contains(contentType)) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        //3.这里说是内容校验，校验的是什么内容呢??
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        //4.文件地址url
        //4.1获取文件名称
        String filename = file.getOriginalFilename();
        //.4.2获取文件后缀名称
        String extension = StringUtils.substringAfterLast(filename, ".");
        //4.3获取目标文件地址
        filename = UUID.randomUUID().toString() + "." + extension;
        File filePath = new File(IMAGE_DIR, filename);
        //5.保存文件
        try {
            file.transferTo(filePath);
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
        return IMAGE_URL + filename;
    }

    @Autowired
    private OSSProperties prop;
    @Autowired
    private OSS client;

    public Map<String, Object> getSignature() {
        try {
            long expireTime = prop.getExpireTime();
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, prop.getMaxFileSize());
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, prop.getDir());

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, Object> respMap = new LinkedHashMap<>();
            respMap.put("accessId", prop.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", prop.getDir());
            respMap.put("host", prop.getHost());
            respMap.put("expire", expireEndTime);
            return respMap;
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }
}
