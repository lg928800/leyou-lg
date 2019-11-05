package com.leyou.item.dto;

import lombok.Data;

import java.util.List;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/1 15:05
 * @description:
 */
@Data
public class BrandDTO {
    private Long id;
    private String name;
    private String image;
    private Character letter;
    private List<Long> cids;
}
