package com.wanpu.myshortlink;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.wanpu.myshortlink.dao.mapper")
@EnableAsync
public class MyshortlinkApplication {

  public static void main(String[] args) {
    SpringApplication.run(MyshortlinkApplication.class, args);
  }

}
