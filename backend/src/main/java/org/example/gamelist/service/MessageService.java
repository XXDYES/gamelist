package org.example.gamelist.service;

import jakarta.annotation.Resource;
import org.example.gamelist.common.Result;
import org.example.gamelist.common.UserContext;
import org.example.gamelist.entity.GameUser;
import org.example.gamelist.entity.Message;
import org.example.gamelist.exception.BusinessException;
import org.example.gamelist.mapper.GameUserMapper;
import org.example.gamelist.mapper.MessageMapper;
import org.example.gamelist.vo.MessageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void addCmtMessage(Integer toId,Integer gameId){
        Message msg = new Message();
        msg.setFromId(UserContext.getCurrentId());
        msg.setToId(toId);
        msg.setType(4);
        msg.setGameId(gameId);
        msg.setMessage("评论了你的游戏");
        messageMapper.insert(msg);
    }
    public void comfirmMessage(Integer infoId){
        Message msg = new Message();
        msg.setId(infoId);
        msg.setStatus(1);
        messageMapper.updateById(msg);
    }
    @Transactional
    public void rejectShare(Integer infoId){
        Message msg = messageMapper.selectById(infoId);
        if (msg == null || !UserContext.getCurrentId().equals(msg.getToId())) {
            throw new BusinessException("该消息不存在");
        }
        msg.setStatus(2);
        messageMapper.updateById(msg);
        Message newMsg = new Message();
        newMsg.setFromId(UserContext.getCurrentId());
        newMsg.setToId(msg.getFromId());
        newMsg.setType(3);
        newMsg.setGameId(msg.getGameId());
        newMsg.setMessage("拒绝了你分享的游戏");
        messageMapper.insert(newMsg);
    }
    @Transactional
    public void acceptShare(Integer infoId){
        Message msg = messageMapper.selectById(infoId);
        if (msg == null || !UserContext.getCurrentId().equals(msg.getToId())) {
            throw new BusinessException("该消息不存在");
        }
        msg.setStatus(1);
        messageMapper.updateById(msg);
        GameUser gameUser = new GameUser();
        gameUser.setUserId(UserContext.getCurrentId());
        gameUser.setGameId(msg.getGameId());
        gameUserMapper.insert(gameUser);
        Message newMsg = new Message();
        newMsg.setFromId(UserContext.getCurrentId());
        newMsg.setToId(msg.getFromId());
        newMsg.setType(3);
        newMsg.setGameId(msg.getGameId());
        newMsg.setMessage("同意了你分享的游戏");
        messageMapper.insert(newMsg);
    }
    public void applyShare(Integer toId,Integer gameId){
        if (gameUserMapper.editAccess(UserContext.getCurrentId(),gameId) > 0){
            throw new BusinessException("已经拥有此游戏");
        }
        if (messageMapper.isApply(UserContext.getCurrentId(),toId,gameId) > 0){
            throw new BusinessException("已申请过该游戏");
        }
        Message msg = new Message();
        msg.setFromId(UserContext.getCurrentId());
        msg.setToId(toId);
        msg.setGameId(gameId);
        msg.setType(1);
        messageMapper.insert(msg);
    }
    @Transactional
    public void acceptApply(Integer infoId){
        Message msg = messageMapper.selectById(infoId);
        if (msg == null || !UserContext.getCurrentId().equals(msg.getToId())) {
            throw new BusinessException("该消息不存在");
        }
        if (msg.getType() == null || msg.getType() != 1) {
            throw new BusinessException("消息类型不匹配");
        }
        if (msg.getStatus() == null || msg.getStatus() != 0) {
            throw new BusinessException("该申请已处理");
        }
        msg.setStatus(1);
        messageMapper.updateById(msg);
        Message newMsg = new Message();
        newMsg.setFromId(UserContext.getCurrentId());
        newMsg.setToId(msg.getFromId());
        newMsg.setGameId(msg.getGameId());
        newMsg.setType(3);
        newMsg.setMessage("同意了你申请的游戏");
        messageMapper.insert(newMsg);
        GameUser gameUser = new GameUser();
        gameUser.setUserId(msg.getFromId());
        gameUser.setGameId(msg.getGameId());
        gameUserMapper.insert(gameUser);
    }
    @Transactional
    public void rejectApply(Integer infoId){
        Message msg = messageMapper.selectById(infoId);
        if (msg == null || !UserContext.getCurrentId().equals(msg.getToId())) {
            throw new BusinessException("该消息不存在");
        }
        if (msg.getType() == null || msg.getType() != 1) {
            throw new BusinessException("消息类型不匹配");
        }
        if (msg.getStatus() == null || msg.getStatus() != 0) {
            throw new BusinessException("该申请已处理");
        }
        msg.setStatus(2);
        messageMapper.updateById(msg);
        Message newMsg = new Message();
        newMsg.setFromId(UserContext.getCurrentId());
        newMsg.setToId(msg.getFromId());
        newMsg.setGameId(msg.getGameId());
        newMsg.setType(3);
        newMsg.setMessage("拒绝了你申请的游戏");
        messageMapper.insert(newMsg);
    }
}
