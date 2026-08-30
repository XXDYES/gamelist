package org.example.gamelist.service;

import jakarta.annotation.Resource;
import org.example.gamelist.common.UserContext;
import org.example.gamelist.dto.AddCmtDTO;
import org.example.gamelist.entity.GameComment;
import org.example.gamelist.exception.BusinessException;
import org.example.gamelist.mapper.CommentMapper;
import org.example.gamelist.vo.CommentVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Resource
    private CommentMapper commentMapper;
    public List<CommentVO> selectComment(){
        return commentMapper.selectComments(UserContext.getCurrentId());
    }
    public void addComment(AddCmtDTO dto){
        if (dto.getGameId() == null) {
            throw new BusinessException("游戏不能为空");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
        if (dto.getContent().trim().length() > 400) {
            throw new BusinessException("评论内容不能超过400字");
        }
        GameComment comment = new GameComment();
        comment.setGameId(dto.getGameId());
        comment.setContent(dto.getContent());
        comment.setUserId(UserContext.getCurrentId());
        comment.setRating(dto.getRating());
        commentMapper.insert(comment);
    }
    public void deleteComment(Integer id){
        Integer num = commentMapper.deleteComment(id,UserContext.getCurrentId());
        if (num == 0){throw new BusinessException("无权删除此评论");}
    }
}
