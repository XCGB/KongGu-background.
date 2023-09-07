package com.hongjie.konggu;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableKnife4j
@MapperScan("com.hongjie.konggu.mapper")
public class KongGuApplication {

    public static void main(String[] args) {
        SpringApplication.run(KongGuApplication.class, args);
    }

}
