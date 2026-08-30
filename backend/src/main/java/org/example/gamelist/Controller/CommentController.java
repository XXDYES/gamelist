package org.example.gamelist.Controller;

import jakarta.annotation.Resource;
import org.example.gamelist.common.Result;
import org.example.gamelist.dto.AddCmtDTO;
import org.example.gamelist.service.CommentService;
import org.example.gamelist.vo.CommentVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {
    @Resource
    private CommentService commentService;
    @GetMapping("/selectcomment")
    public Result<List<CommentVO>> selectComment(){
        return Result.success(commentService.selectComment());
    }
    @PostMapping("/addcomment")
    public Result<?> addComment(@RequestBody AddCmtDTO dto){
        commentService.addComment(dto);
        return Result.success();
    }
    @GetMapping("/deletecomment")
    public Result<?> deleteComment(@RequestParam("cmtId") Integer cmtId){
        commentService.deleteComment(cmtId);
        return Result.success();
    }
}
