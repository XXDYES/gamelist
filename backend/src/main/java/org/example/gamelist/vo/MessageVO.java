package org.example.gamelist.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MessageVO {
    private Integer id;
    private Integer gameId;
    private String gameName;
    private String userName;
    private String message;
    private Integer type;
    private LocalDate createAt;
}
