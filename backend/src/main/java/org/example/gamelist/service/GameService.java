package org.example.gamelist.service;

import jakarta.annotation.Resource;
import org.example.gamelist.client.AiClient;
import org.example.gamelist.common.UserContext;
import org.example.gamelist.dto.GameInfoDTO;
import org.example.gamelist.entity.Game;
import org.example.gamelist.entity.GameUser;
import org.example.gamelist.exception.BusinessException;
import org.example.gamelist.mapper.GameMapper;
import org.example.gamelist.mapper.GameUserMapper;
import org.example.gamelist.vo.GameVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class GameService {
    @Resource
    private GameMapper gameMapper;
    @Resource
    private GameUserMapper gameUserMapper;
    @Resource
    private AiClient aiClient;  // ← 新增：注入 AI 客户端
    public List<GameVO> getGameList(Integer userId){
        try{
            List<GameVO> gameList = gameMapper.selectGamesByUserId(userId);
            return gameList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public GameInfoDTO getGameInfoFromAI(String gameName) {
        // 参数校验
        if (gameName == null || gameName.trim().isEmpty()) {
            throw new BusinessException("游戏名称不能为空");
        }
        if (gameName.length() > 20) {
            throw new BusinessException("游戏名称不能超过20个字符");
        }
        // 调用 AI 客户端
        return aiClient.getGameInfo(gameName.trim());
    }
    @Transactional
    public void addGame(Game game){
        gameMapper.insert(game);
        GameUser gameUser = new GameUser();
        gameUser.setUserId(UserContext.getCurrentId());
        gameUser.setGameId(game.getId());
        gameUser.setPlayed(0);
        gameUser.setRating(0);
        gameUser.setAddDate(LocalDate.now());
        gameUserMapper.insert(gameUser);
    }
    public boolean updatePlayed(Integer played,Integer gameId){
        int row = gameUserMapper.updatePlayed(played,UserContext.getCurrentId(),gameId);
        if (row > 0){
            return true;
        }else {return false;}
    }
    public boolean deleteGame(Integer gameId){
        int row = gameUserMapper.deleteGame(UserContext.getCurrentId(),gameId);
        if (row > 0){
            return true;
        }else {return false;}
    }
    public boolean updateRating(Integer rating,Integer gameId){
        int row = gameUserMapper.updateRating(rating,UserContext.getCurrentId(),gameId);
        if (row > 0){
            return true;
        }else {return false;}
    }
    @Transactional
    public boolean editGame(Game game){
        int row = gameUserMapper.editAccess(UserContext.getCurrentId(),game.getId());
        if (row > 0){
            gameMapper.updateById(game);
            return true;
        }else {return false;}
    }
}

