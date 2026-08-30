package org.example.gamelist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.gamelist.entity.GameComment;
import org.example.gamelist.vo.CommentVO;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<GameComment> {
    @Select("SELECT c.*, u.username " +
            "FROM game_comment c " +
            "JOIN users u ON c.user_id = u.id " +
            "WHERE c.game_id IN ( " +
            "    SELECT ug.game_id FROM user_game ug WHERE ug.user_id = #{userId} " +
            ") " +
            "ORDER BY c.game_id, c.create_at DESC")
    List<CommentVO> selectComments(@Param("userId") Integer userId);
    @Delete("DELETE FROM game_comment c " +
            "WHERE c.id = #{commentId} " +
            "AND (c.user_id = #{userId} OR EXISTS (" +
            "    SELECT 1 FROM user_game ug " +
            "    WHERE ug.user_id = #{userId} AND ug.game_id = c.game_id" +
            "))")
    int deleteComment(@Param("commentId") Integer commentId,
                      @Param("userId") Integer userId);
}
