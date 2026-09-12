package org.example.gamelist.Controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.gamelist.common.JwtUtil;
import org.example.gamelist.common.Result;
import org.example.gamelist.common.UserContext;
import org.example.gamelist.entity.User;
import org.example.gamelist.exception.BusinessException;
import org.example.gamelist.service.LoginService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class UserController {
    @Resource
    private LoginService loginService;
    @Resource
    private JwtUtil jwtUtil;

    @RequestMapping(value = "/login", method = {RequestMethod.POST, RequestMethod.GET})
        public Result<String> login(@RequestBody User user){
        try {
            // 1. 调用 Service 登录，返回 User 对象
            User dbUser = loginService.login(user);
            log.info(String.format("%s登录了",dbUser.getUsername()));
            // 2. 生成 JWT Token
            String token = jwtUtil.generateToken(dbUser);

            // 3. 返回 Token（String 类型）
            return Result.success(token);  // ✅ Result<String>
        } catch (BusinessException e) {
            return Result.error(e.getMsg());
        }
    }
    @GetMapping("/user/info")
    public Result<User> getUserInfo() {
        Integer userId = UserContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        // 从数据库查询最新用户信息
        User user = loginService.getUserById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 不返回密码等敏感信息
        user.setPassword("就不告诉你");
        return Result.success(user);
    }
}
