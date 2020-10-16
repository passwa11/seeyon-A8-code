package com.ncbi.medhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//激活定时任务
@EnableScheduling
public class MedhubApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedhubApplication.class, args);
    }

}
