package com.hongjie.konggu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongjie.konggu.model.domain.Post;
import com.hongjie.konggu.service.PostService;
import com.hongjie.konggu.mapper.PostMapper;
import org.springframework.stereotype.Service;

/**
* @author WHJ
* @description 针对表【post(帖子表)】的数据库操作Service实现
* @createDate 2023-06-17 16:31:33
*/
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
    implements PostService{

}




