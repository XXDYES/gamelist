package org.example.gamelist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.gamelist.entity.Friend;
import org.example.gamelist.vo.FriendVO;

import java.util.List;

@Mapper
public interface FriendMapper extends BaseMapper<Friend> {
    @Select("SELECT u.id, u.username AS friend_name, f.create_at " +
            "FROM users u " +
            "JOIN ( " +
            "    SELECT friend_id AS fid, create_at FROM friend WHERE user_id = #{userId} " +
            "    UNION " +
            "    SELECT user_id AS fid, create_at FROM friend WHERE friend_id = #{userId} " +
            ") f ON u.id = f.fid")
    List<FriendVO> searchFriend (@Param("userId")Integer userId);
    @Select("SELECT COUNT(*) FROM friend " +
            "WHERE (user_id = #{userId} AND friend_id = #{friendId}) " +
            "   OR (user_id = #{friendId} AND friend_id = #{userId})")
    int countFriendship(@Param("userId") Integer userId,
                        @Param("friendId") Integer friendId);
    @Delete("Delete FROM friend " +
            "WHERE (user_id = #{userId} AND friend_id = #{friendId}) " +
            "   OR (user_id = #{friendId} AND friend_id = #{userId})")
    int deleteFriend(@Param("userId") Integer userId,
                     @Param("friendId") Integer friendId);
}
