package org.example.gamelist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.example.gamelist.entity.GameUser;

public interface GameUserMapper extends BaseMapper<GameUser> {
    @Update("UPDATE user_game SET played = #{played} WHERE user_id = #{userId} AND game_id = #{gameId}")
    int updatePlayed(@Param("played")Integer played,
                     @Param("userId")Integer userId,
                     @Param("gameId")Integer gameId);
    @Delete("DELETE from user_game WHERE user_id = #{userId} AND game_id = #{gameId}")
    int deleteGame(@Param("userId") Integer userId,
                   @Param("gameId") Integer gameId);
}
