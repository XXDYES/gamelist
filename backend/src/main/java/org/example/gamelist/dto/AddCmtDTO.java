package org.example.gamelist.dto;

import lombok.Data;

@Data
public class AddCmtDTO {
    private Integer gameId;
    private String content;
    private Integer rating;
}
