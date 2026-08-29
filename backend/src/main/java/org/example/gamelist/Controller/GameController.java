package org.example.gamelist.Controller;

import jakarta.annotation.Resource;
import org.example.gamelist.common.Result;
import org.example.gamelist.common.UserContext;
import org.example.gamelist.dto.GameInfoDTO;
import org.example.gamelist.entity.Game;
import org.example.gamelist.exception.BusinessException;
import org.example.gamelist.service.GameService;
import org.example.gamelist.vo.GameVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class GameController {
    @Resource
    private GameService gameService;
    @RequestMapping(value = "/getlist", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<?> getList(){
        try {
            Integer userId = UserContext.getCurrentId();
            List<GameVO> gameList = gameService.getGameList(userId);
            return Result.success(gameList);
        }catch (BusinessException e){
            return Result.error(e.getMsg());
        }
    }
    @GetMapping("/ai")
    public Result<GameInfoDTO> getGameInfoFromAI(@RequestParam String gameName) {
        GameInfoDTO data = gameService.getGameInfoFromAI(gameName);
        return Result.success(data);
    }
    @PostMapping("/addgame")
    public Result<?> addGame(@RequestBody Game game){
        gameService.addGame(game);
        return Result.success();
    }
    @PostMapping("/setplayed")
    public Result<?> setPlayed(@RequestBody Map<String,Integer>data){
        Integer played = data.get("played");
        Integer gameId = data.get("gameId");
        boolean update = gameService.updatePlayed(played,gameId);
        if (update){
            return Result.success();
        }else {return Result.error("更新失败");}
    }
    @GetMapping("/deletegame")
    public Result<?> deleteGame(@RequestParam Integer gameId){
        boolean delete = gameService.deleteGame(gameId);
        if (delete){
            return Result.success();
        }else {return Result.error("删除失败");}
    }
    @PostMapping("/setrating")
    public Result<?> setRating(@RequestBody Map<String,Integer>data){
        Integer rating = data.get("rating");
        Integer gameId = data.get("gameId");
        boolean update = gameService.updateRating(rating,gameId);
        if (update){
            return Result.success();
        }else {return Result.error("更新失败");}
    }
    @PostMapping("/editgame")
    public Result<?> editGame(@RequestBody Game game){
        boolean res = gameService.editGame(game);
        if (res){
            return Result.success();
        }else {return Result.error("无权修改此信息");}
    }
}
