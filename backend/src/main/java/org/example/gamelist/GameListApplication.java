package org.example.gamelist;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@MapperScan("org.example.gamelist.mapper")
@SpringBootApplication
public class GameListApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameListApplication.class, args);
    }

}