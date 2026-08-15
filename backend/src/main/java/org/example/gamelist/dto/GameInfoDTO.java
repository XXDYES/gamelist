package org.example.gamelist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@TableName("games")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameInfoDTO {
//    @TableId(type = IdType.AUTO)
//    private Integer id;
    private String name;
    private String company;
    private String platform;
    private String type;
    private String cover;
    private String info;
    private String price;
    private String mcRating;
    private String releaseDate;
}
