package org.example.gamelist.Controller;

import jakarta.annotation.Resource;
import org.example.gamelist.common.Result;
import org.example.gamelist.entity.Register;
import org.example.gamelist.exception.BusinessException;
import org.example.gamelist.service.LoginService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisterController {
    @Resource
    private LoginService loginService;
    @RequestMapping(value = "/register", method = {RequestMethod.POST, RequestMethod.GET})
        public Result register(@RequestBody Register register){
        try {
            loginService.register(register);
            return Result.success("注册成功");
        }catch (BusinessException e){
            return Result.error(e.getMsg());
        }
    }
}
