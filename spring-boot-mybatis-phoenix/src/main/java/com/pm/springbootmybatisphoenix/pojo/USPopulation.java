package com.pm.springbootmybatisphoenix.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author pengcheng
 * @version V1.0
 * @description
 * @date 2020/04/29 00:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class USPopulation {
    private String state;

    private String city;

    private Integer population;
}
