package org.example.gamelist.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FriendVO {
    private Integer id;
    private String friendName;
    private LocalDate createAt;
}
