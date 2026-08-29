package org.example.gamelist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.gamelist.entity.FriendRequest;

import java.util.List;

@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {
    @Select("SELECT * FROM friend_request "+
    "WHERE to_id = #{toId} AND status = 0")
    List<FriendRequest> getFriQuest(@Param("toId") Integer toId);
    @Update("UPDATE friend_request SET status = 1 WHERE id = #{infoId} AND status = 0")
    int agreeQuest(@Param("infoId") Integer infoId);
    @Update("UPDATE friend_request SET status = 2 WHERE id = #{infoId} AND status = 0")
    int rejectQuest(@Param("infoId") Integer infoId);
    @Select("SELECT COUNT(*) FROM friend_request " +
            "WHERE status = 0 " +
            "AND ((from_id = #{userId} AND to_id = #{friendId}) " +
            "OR (from_id = #{friendId} AND to_id = #{userId}))")
    int ifExistQuest(@Param("userId") Integer userId, @Param("friendId") Integer friendId);
}
