package org.example.gamelist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.gamelist.entity.Register;
import org.example.gamelist.entity.User;

@Mapper
public interface RegisterMapper extends BaseMapper<Register> {
    @Select("SELECT id, username, password FROM users WHERE username = #{username}")
    User selectByUsername(String username);
}
