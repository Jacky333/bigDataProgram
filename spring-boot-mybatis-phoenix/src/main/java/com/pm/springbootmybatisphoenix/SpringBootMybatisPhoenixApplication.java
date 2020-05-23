package com.pm.springbootmybatisphoenix;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.pm.springbootmybatisphoenix"})
@MapperScan(basePackages = {"com.pm.springbootmybatisphoenix.mapper"})
public class SpringBootMybatisPhoenixApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootMybatisPhoenixApplication.class, args);
    }

}
