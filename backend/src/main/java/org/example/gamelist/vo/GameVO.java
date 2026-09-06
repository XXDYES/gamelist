package org.example.gamelist.vo;


import lombok.Data;

import java.sql.Date;
@Data
public class GameVO {
    // ========== games 表字段 ==========
    private Integer id;
    private String name;
    private String company;
    private String platform;
    private String type;
    private String cover;
    private String info;
    private String price;
    private String mcRating;      // 对应 mc_rating
    private String releaseDate;   // 对应 release_date
    // ========== user_game 表字段（关联查询） ==========
    private Integer played;       // 0=待玩 1=已玩
    private Integer rating;       // 评分 1-5
    private Integer userId;       // 对应 user_id
    private Integer gameId;       // 对应 game_id
    private Date addDate;         // 对应 add_date 添加日期
}
