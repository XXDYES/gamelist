package org.example.gamelist.Controller;

import jakarta.annotation.Resource;
import org.example.gamelist.common.Result;
import org.example.gamelist.entity.Friend;
import org.example.gamelist.entity.FriendRequest;
import org.example.gamelist.entity.User;
import org.example.gamelist.service.FriendService;
import org.example.gamelist.vo.FriendVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FriendController {
    @Resource
    private FriendService friendService;
    @GetMapping("/friendlist")
    public Result<List<FriendVO>> friendList(){
        return Result.success(friendService.searchFriend());
    }
    @GetMapping("/friquestlist")
    public Result<List<FriendRequest>> friQuestList(){
        return Result.success(friendService.seachFriQuset());
    }
    @GetMapping("/agreequest")
    public Result<?> agreeQuest(@RequestParam Integer infoId){
        friendService.agreeQuest(infoId);
        return Result.success();
    }
    @GetMapping("/rejectquest")
    public Result<?> rejectQuest(@RequestParam Integer infoId){
        friendService.rejectQuest(infoId);
        return Result.success();
    }
    @GetMapping("/searchuser")
    public Result<Integer> searchUser(@RequestParam String name){
        return Result.success(friendService.searchUser(name).getId());
    }
    @GetMapping("/sendquest")
    public Result<?> sendquset(@RequestParam Integer toId, @RequestParam(required = false) String msg){
        friendService.sendQuest(toId,msg);
        return Result.success();
    }
    @GetMapping("deletefriend")
    public Result<?> deleteFriend(@RequestParam Integer id){
        friendService.deleteFriend(id);
        return Result.success();
    }
}
