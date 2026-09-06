package org.example.gamelist.service;

import jakarta.annotation.Resource;
import org.example.gamelist.common.UserContext;
import org.example.gamelist.entity.Friend;
import org.example.gamelist.entity.FriendRequest;
import org.example.gamelist.entity.User;
import org.example.gamelist.exception.BusinessException;
import org.example.gamelist.mapper.FriendMapper;
import org.example.gamelist.mapper.FriendRequestMapper;
import org.example.gamelist.mapper.GameMapper;
import org.example.gamelist.mapper.UserMapper;
import org.example.gamelist.vo.FriendVO;
import org.example.gamelist.vo.GameVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FriendService {
    @Resource
    private FriendMapper friendMapper;
    @Resource
    private FriendRequestMapper friendRequestMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private GameMapper gameMapper;
    public List<FriendVO> searchFriend(){
        return friendMapper.searchFriend(UserContext.getCurrentId());
    }
    public  List<FriendRequest> seachFriQuset(){
        return friendRequestMapper.getFriQuest(UserContext.getCurrentId());
    }
    @Transactional
    public void agreeQuest(Integer infoId){
        FriendRequest req = friendRequestMapper.selectById(infoId);
        if (req == null || !UserContext.getCurrentId().equals(req.getToId())){
            throw new BusinessException("申请不存在");
        }
        if (req.getStatus() != 0){
            throw new BusinessException("该申请已处理");
        }
        int rows = friendRequestMapper.agreeQuest(infoId);
        if (rows <= 0){
            throw new BusinessException("该申请已处理");
        }
        Friend newFriend = new Friend();
        newFriend.setUserId(UserContext.getCurrentId());
        newFriend.setFriendId(req.getFromId());
        friendMapper.insert(newFriend);
    }
    @Transactional
    public void rejectQuest(Integer infoId){
        FriendRequest req = friendRequestMapper.selectById(infoId);
        if (req == null || !UserContext.getCurrentId().equals(req.getToId())){
            throw new BusinessException("申请不存在");
        }
        if (req.getStatus() != 0){
            throw new BusinessException("该申请已处理");
        }
        int rows = friendRequestMapper.rejectQuest(infoId);
        if (rows <= 0){
            throw new BusinessException("该申请已处理");
        }
    }
    public User searchUser(String name){
        User user = userMapper.selectByUsername(name);
        if (user != null){
            return user;
        }else {throw new BusinessException("用户不存在");}
    }
    public void sendQuest(Integer toId,String msg){
        if (toId == null){
            throw new BusinessException("目标用户不能为空");
        }
        if (toId.equals(UserContext.getCurrentId())){
            throw new BusinessException("不能添加自己为好友");
        }
        if (friendMapper.countFriendship(UserContext.getCurrentId(), toId) > 0){
            throw new BusinessException("你们已经是好友了");
        }
        if (friendRequestMapper.ifExistQuest(UserContext.getCurrentId(),toId) != 0){
            throw new BusinessException("该申请已存在");
        }
        FriendRequest quest = new FriendRequest();
        quest.setFromId(UserContext.getCurrentId());
        quest.setToId(toId);
        quest.setMessage(msg);
        quest.setFromName(UserContext.getCurrentUsername());
        quest.setStatus(0);
        friendRequestMapper.insert(quest);
    }
    public void deleteFriend(Integer id){
        int rows = friendMapper.deleteFriend(UserContext.getCurrentId(),id);
        if (rows <= 0){
            throw new BusinessException("该用户不是你的好友");
        }
    }
    public List<GameVO> getFriGame(Integer friId){
        Integer count = friendMapper.countFriendship(UserContext.getCurrentId(),friId);
        if(count>0){
            return gameMapper.selectGamesByUserId(friId);
        }else {throw new BusinessException("无权访问非好友主页");}
    }
    public User getFriInfo(Integer friId){
        return userMapper.selectById(friId);
    }
}
