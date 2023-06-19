package com.hongjie.konggu;

import com.hongjie.konggu.mapper.UsersMapper;
import com.hongjie.konggu.model.domain.Users;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class KongGuApplicationTests {

    @Resource
    private UsersMapper usersMapper;

    @Test
    void contextLoads() {
        System.out.println("select from users:");
        List<Users> users = usersMapper.selectList(null);
        System.out.println(users);
    }

}
