package org.example.gamelist.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.gamelist.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper extends BaseMapper<User>{
    @Select("SELECT id, username, password FROM users WHERE username = #{username}")
    User selectByUsername(String username);
}

