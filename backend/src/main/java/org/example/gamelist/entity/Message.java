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
    private Integer type;     // 1=请求共享，2=推荐，3=好友信息，4=评论信息
    private Integer gameId;
    private String message;
    private Integer status;   // 0=待处理(仅需选择类) 1=已同意 2=已拒绝；通知类恒 0
    private LocalDate createAt;
}
