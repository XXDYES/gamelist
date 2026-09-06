package org.example.gamelist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("messages")
public class Message {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer fromId;
    private Integer toId;
    private Integer type;
    private Integer gameId;
    private String message;
    private LocalDate createAt;
}
