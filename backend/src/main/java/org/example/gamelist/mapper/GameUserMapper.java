package org.example.gamelist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.example.gamelist.entity.GameUser;

@Mapper
public interface GameUserMapper extends BaseMapper<GameUser> {
    @Update("UPDATE user_game SET played = #{played} WHERE user_id = #{userId} AND game_id = #{gameId}")
    int updatePlayed(@Param("played")Integer played,
                     @Param("userId")Integer userId,
                     @Param("gameId")Integer gameId);
    @Delete("DELETE from user_game WHERE user_id = #{userId} AND game_id = #{gameId}")
    int deleteGame(@Param("userId") Integer userId,
                   @Param("gameId") Integer gameId);
    @Update("UPDATE user_game SET rating = #{rating} WHERE user_id = #{userId} AND game_id = #{gameId}")
    int updateRating(@Param("rating")Integer rating,
                     @Param("userId")Integer userId,
                     @Param("gameId")Integer gameId);
    @Select("SELECT COUNT(*) FROM user_game WHERE user_id = #{userId} AND game_id = #{gameId}")
    int editAccess(@Param("userId") Integer userId, @Param("gameId") Integer gameId);
}
