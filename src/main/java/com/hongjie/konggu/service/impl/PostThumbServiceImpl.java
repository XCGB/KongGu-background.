package com.hongjie.konggu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.model.domain.Post;
import com.hongjie.konggu.service.PostService;
import com.hongjie.konggu.service.PostThumbService;
import com.hongjie.konggu.model.domain.PostThumb;
import com.hongjie.konggu.mapper.PostThumbMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
* @author WHJ
* @description 针对表【post_thumb(帖子点赞表)】的数据库操作Service实现
* @createDate 2023-06-17 16:32:41
*/
@Service
public class PostThumbServiceImpl extends ServiceImpl<PostThumbMapper, PostThumb>
    implements PostThumbService {

    @Resource
    private PostService postService;

    @Override
    public long doThumb(Long userId, Long postId) {
        //判断帖子是否存在
        Post post = postService.getById(postId);
        if(post == null){
            throw new BusinessException(ErrorCode.NO_FOUND_ERROR);
        }
        //判断是否已点赞
        synchronized (String.valueOf(userId).intern()){
            return doThumbInner(userId,postId);
        }
    }

    /**
     * 封装了事务方法
     * @param userId
     * @param postId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int doThumbInner(long userId,long postId){
        PostThumb postThumb = new PostThumb();
        postThumb.setUserId(userId);
        postThumb.setPostId(postId);
        QueryWrapper<PostThumb> wrapper = new QueryWrapper(postThumb);
        int count = (int) this.count(wrapper);
        //已点赞
        if(count > 0){
            boolean result = this.remove(wrapper);
            if(result){
                //帖子点赞数量-1
                postService.update()
                        .eq("id",postId)
                        .setSql("thumbNum = thumbNum - 1")
                        .update();
                return result ? -1 : 0;
            } else{
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }else{
            //未点赞
            boolean result = this.save(postThumb);
            if(result){
                //帖子点赞数量+1
                postService.update()
                        .eq("id",postId)
                        .setSql("thumbNum = thumbNum + 1")
                        .update();
                return result ? 1 : 0;
            } else{
                throw  new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }
}



