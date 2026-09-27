package org.example.gamelist.Controller;

import jakarta.annotation.Resource;
import org.example.gamelist.common.Result;
import org.example.gamelist.service.MessageService;
import org.example.gamelist.vo.MessageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MessageController {
    @Resource
    private MessageService messageService;
    @GetMapping("/addsharemsg")
    public Result<?> addShareMsg(@RequestParam("toId")Integer toId,
                                 @RequestParam("gameId")Integer gameId,
                                 @RequestParam("message")String message){
        messageService.addShareMessage(toId,gameId,message);
        return Result.success();
    }
    @GetMapping("/getmessage")
    public Result<List<MessageVO>> getMessage(){
        return Result.success(messageService.getMessage());
    }
    @GetMapping("/addcmtmessage")
    public Result<?> addCmtMessage(@RequestParam("toId")Integer toId,
                                   @RequestParam("gameId")Integer gameId){
        messageService.addCmtMessage(toId,gameId);
        return Result.success();
    }
    @GetMapping("/comfirmmessage")
    public Result<?> comfirmMessage(@RequestParam("infoId")Integer infoId){
        messageService.comfirmMessage(infoId);
        return Result.success();
    }
    @GetMapping("/rejectshare")
    public Result<?> rejectShare(@RequestParam("infoId")Integer infoId){
        messageService.rejectShare(infoId);
        return Result.success();
    }
    @GetMapping("/acceptshare")
    public Result<?> acceptShare(@RequestParam("infoId")Integer infoId){
        messageService.acceptShare(infoId);
        return Result.success();
    }
    @GetMapping("/applyshare")
    public Result<?> applyShare(@RequestParam("toId")Integer toId,
                                @RequestParam("gameId")Integer gameId){
        messageService.applyShare(toId,gameId);
        return Result.success();
    }
    @GetMapping("/acceptapply")
    public Result<?> acceptApply(@RequestParam("infoId")Integer infoId){
        messageService.acceptApply(infoId);
        return Result.success();
    }
    @GetMapping("rejectapply")
    public Result<?> rejectApply(@RequestParam("infoId")Integer infoId){
        messageService.rejectApply(infoId);
        return Result.success();
    }
}
