package org.example.gamelist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Date;

@Data
@TableName("games")
public class Game {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String company;
    private String platform;
    private String type;
    private String cover;
    private String info;
    private String price;
    private String mcRating;
    private String releaseDate;
}
