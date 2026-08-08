package org.example.gamelist.service;
import jakarta.annotation.Resource;
import org.example.gamelist.common.EncodeUtil;
import org.example.gamelist.entity.Register;
import org.example.gamelist.entity.User;
import org.example.gamelist.exception.BusinessException;
import org.example.gamelist.mapper.RegisterMapper;
import org.example.gamelist.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service

public class LoginService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private RegisterMapper registerMapper;
    public User login(User user) {
        // 1. 校验参数非空
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }

        // 2. 查询用户
        User dbUser = userMapper.selectByUsername(user.getUsername());
        if (dbUser == null) {
            throw new BusinessException("用户名不存在");
        }

        // 3. 校验密码
        if (!EncodeUtil.matches(user.getPassword(), dbUser.getPassword())) {
            throw new BusinessException("密码错误");
        }

        return dbUser;
    }
    public void register(Register register){
        if (register.getUsername() == null ||register.getUsername().isEmpty()){
            throw new BusinessException("账号不能为空");
        }
        if (register.getPassword() == null ||register.getPassword().isEmpty()){
            throw new BusinessException("密码不能为空");
        }
        if (register.getRepassword() == null ||register.getRepassword().isEmpty()){
            throw new BusinessException("需要再次输入密码");
        }
        if(!register.getPassword().equals(register.getRepassword())){
            throw new BusinessException("密码不相同");
        }
        User existingUser = userMapper.selectByUsername(register.getUsername());
        if (existingUser != null){
            throw new BusinessException("用户名已经存在");
        }
        String encodedPassword = EncodeUtil.encode(register.getPassword());
        register.setPassword(encodedPassword);
        int row=registerMapper.insert(register);
        if (row <= 0) {
            throw new BusinessException("注册失败，数据库插入异常");
        }
    }
    public User getUserById(Integer id) {
        return userMapper.selectById(id);
    }
}

