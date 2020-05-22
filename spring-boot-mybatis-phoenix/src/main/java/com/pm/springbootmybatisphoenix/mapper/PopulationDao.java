package com.pm.springbootmybatisphoenix.mapper;

import com.pm.springbootmybatisphoenix.pojo.USPopulation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author pengcheng
 * @version V1.0
 * @description
 * @date 2020/04/29 00:06
 */
@Mapper
@Component
public interface PopulationDao {
    @Select("SELECT * from us_population")
    List<USPopulation> queryAll();

    @Insert("UPSERT INTO us_population VALUES( #{state}, #{city}, #{population} )")
    void save(USPopulation USPopulation);

    @Select("SELECT * FROM us_population WHERE state=#{state} AND city = #{city}")
    USPopulation queryByStateAndCity(String state, String city);


    @Delete("DELETE FROM us_population WHERE state=#{state} AND city = #{city}")
    void deleteByStateAndCity(String state, String city);
}
