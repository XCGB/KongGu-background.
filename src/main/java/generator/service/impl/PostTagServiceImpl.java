package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.PostTag;
import generator.service.PostTagService;
import generator.mapper.PostTagMapper;
import org.springframework.stereotype.Service;

/**
* @author WHJ
* @description 针对表【post_tag(帖子标签关联表)】的数据库操作Service实现
* @createDate 2023-06-23 20:21:43
*/
@Service
public class PostTagServiceImpl extends ServiceImpl<PostTagMapper, PostTag>
    implements PostTagService{

}




