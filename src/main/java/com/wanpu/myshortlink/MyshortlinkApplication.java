package com.wanpu.myshortlink;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wanpu.myshortlink.dao.mapper")
public class MyshortlinkApplication {

  public static void main(String[] args) {
    SpringApplication.run(MyshortlinkApplication.class, args);
  }

}
