package com.leyou.item.dto;

import lombok.Data;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/10/31 20:03
 * @description:
 */
@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private Long parentId;
    private Boolean isParent;
    private Integer sort;
}
