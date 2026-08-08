package org.example.gamelist.Controller;

import jakarta.annotation.Resource;
import org.example.gamelist.common.Result;
import org.example.gamelist.dto.GameInfoDTO;
import org.example.gamelist.exception.BusinessException;
import org.example.gamelist.service.GameService;
import org.example.gamelist.vo.GameVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GameController {
    @Resource
    private GameService gameService;
    @RequestMapping(value = "/getlist", method = {RequestMethod.POST, RequestMethod.GET})
    public Result getList(@RequestParam("userId")Integer userId){
        try {
            List<GameVO> gameList = gameService.getGameList(userId);
            return Result.success(gameList);
        }catch (BusinessException e){
            return Result.error(e.getMsg());
        }
    }
    @GetMapping("/ai")
    public Result<GameInfoDTO> getGameInfoFromAI(@RequestParam String name) {
        GameInfoDTO data = gameService.getGameInfoFromAI(name);
        return Result.success(data);
    }
}
