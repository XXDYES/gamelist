package org.example.gamelist.entity;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("users")
public class User {
    private Integer id;
    private String username;
    private String password;
    private String signature;
}
