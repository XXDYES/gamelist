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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    @Resource
    private StringRedisTemplate stringRedisTemplate;
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
        String key = "ai:limit:" + UserContext.getCurrentId() + ":" + LocalDate.now();
        Long used = stringRedisTemplate.opsForValue().increment(key);   // 原子 +1，返回加完后的值
        if(used == 1){stringRedisTemplate.expire(key, Duration.ofHours(24));}
        if (used != null && used > 100) {
            throw new BusinessException("今日 AI 查询次数已用完，请明天再试");
        }
        // 调用 AI 客户端
        GameInfoDTO dto = aiClient.getGameInfo(gameName.trim());
        if (dto == null) {
            throw new BusinessException("AI 暂时没有返回结果，请稍后重试");
        }
        // AI 判定输入与游戏无关时，只原样返回 name，其余字段全为空壳
        if (isNotGame(dto)) {
            throw new BusinessException("未识别为游戏名称，请确认后重新输入");
        }
        return dto;
    }
    /** 除 name 外全部为空、或全部是"暂无" → 输入与游戏无关 */
    private boolean isNotGame(GameInfoDTO dto) {
        return isMissing(dto.getInfo())
                && isMissing(dto.getCompany())
                && isMissing(dto.getPlatform())
                && isMissing(dto.getType());
    }
    /** 空串、空白串、以及"暂无"都算作没有内容 */
    private boolean isMissing(String s) {
        return s == null || s.trim().isEmpty() || "暂无".equals(s.trim());
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

