package org.example.gamelist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.gamelist.entity.Message;
import org.example.gamelist.vo.MessageVO;

import java.util.List;

public interface MessageMapper extends BaseMapper<Message> {
    @Select("SELECT m.id, g.name AS game_name, u.username AS user_name, m.game_id, " +
            "m.message, m.type, m.create_at " +
            "FROM messages m " +
            "JOIN users u ON u.id = m.from_id " +
            "LEFT JOIN games g ON m.game_id = g.id " +
            "WHERE m.to_id = #{toId} AND m.status = 0 " +
            "ORDER BY m.id DESC")
    List<MessageVO> getMessages(@Param("toId") Integer toId);
    @Select("SELECT COUNT(*) FROM messages " +
            "WHERE from_id = #{fromId} " +
            "  AND to_id = #{toId} " +
            "  AND game_id = #{gameId} " +
            "  AND type = 2 " +
            "  AND status = 0")
    int isShared(@Param("fromId") Integer fromId,
                          @Param("toId") Integer toId,
                          @Param("gameId") Integer gameId);
}
