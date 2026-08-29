package org.example.gamelist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("friend_request")
public class FriendRequest {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer fromId;
    private Integer toId;
    private Integer status;
    private LocalDate createAt;
    private String message;
    private String fromName;
}
