package org.example.gamelist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("game_comment")
public class GameComment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer gameId;
    private Integer userId;
    private String content;
    private LocalDate createAt;
    private Integer parentId;
    private Integer rating;
}
