package org.example.gamelist.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CommentVO {
    private Integer id;
    private Integer gameId;
    private Integer userId;
    private String content;
    private LocalDate createAt;
    private Integer parentId;
    private Integer rating;
    private String username;   // 从 users 表带出来的
}
