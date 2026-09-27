package org.example.gamelist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.gamelist.entity.Game;
import org.example.gamelist.vo.GameVO;

import java.util.List;

@Mapper
public interface GameMapper extends BaseMapper<Game> {
    @Select("SELECT g.*, ug.played, ug.rating ,ug.user_id,ug.add_date,ug.game_id " +
            "FROM games g " +
            "INNER JOIN user_game ug ON g.id = ug.game_id " +
            "WHERE ug.user_id = #{userId}")
    List<GameVO> selectGamesByUserId(@Param("userId") Integer userId);

    /** 只取游戏名和评分，供 AI 提示词里的游戏库使用 */
    @Select("SELECT g.name, ug.rating " +
            "FROM games g " +
            "INNER JOIN user_game ug ON g.id = ug.game_id " +
            "WHERE ug.user_id = #{userId} " +
            "ORDER BY ug.rating DESC")
    List<GameVO> selectGameBriefByUserId(@Param("userId") Integer userId);
}
