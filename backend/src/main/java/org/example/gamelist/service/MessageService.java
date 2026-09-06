package org.example.gamelist.service;

import jakarta.annotation.Resource;
import org.example.gamelist.common.Result;
import org.example.gamelist.common.UserContext;
import org.example.gamelist.entity.Message;
import org.example.gamelist.exception.BusinessException;
import org.example.gamelist.mapper.GameUserMapper;
import org.example.gamelist.mapper.MessageMapper;
import org.example.gamelist.vo.MessageVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private GameUserMapper gameUserMapper;
    public void addShareMessage(Integer toId,Integer gameId,String message){
        if (gameUserMapper.editAccess(toId,gameId) > 0){
            throw new BusinessException("对方已经拥有此游戏");
        }
        if (messageMapper.isShared(UserContext.getCurrentId(),toId,gameId) > 0){
            throw new BusinessException("已分享过该游戏");
        }
        Message msg = new Message();
        msg.setFromId(UserContext.getCurrentId());
        msg.setToId(toId);
        msg.setType(2);
        msg.setGameId(gameId);
        msg.setMessage(message);
        messageMapper.insert(msg);
    }
    public List<MessageVO> getMessage(){
        return messageMapper.getMessages(UserContext.getCurrentId());
    }
}
