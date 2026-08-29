package org.example.gamelist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Date;
import java.time.LocalDateTime;
@Data
@TableName("friend")
public class Friend {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer friendId;
    private LocalDateTime createAt;
}
