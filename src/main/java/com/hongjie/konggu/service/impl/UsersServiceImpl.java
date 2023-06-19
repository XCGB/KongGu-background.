package com.hongjie.konggu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.model.domain.Users;
import com.hongjie.konggu.model.domain.request.UserLoginRequest;
import com.hongjie.konggu.model.domain.request.UserRegisterRequest;
import com.hongjie.konggu.service.UsersService;
import com.hongjie.konggu.mapper.UsersMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hongjie.konggu.constant.UserConstant.*;

/**
* @author WHJ
* @description 针对表【users(用户表)】的数据库操作Service实现
* @createDate 2023-06-17 16:27:43
*/
@Service
@Slf4j
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{

    @Resource
    private UsersMapper usersMapper;

    // 盐值
    private static final String SALT = "Whj";

    private void validateParameterLength(String userAccount, String password) {
        if (userAccount.length() < 10){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度小于10");
        }
        if (password.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度小于8");
        }
    }


    private void validateUserAccountSpecialCharacters(String userAccount) {
        String volidPattern = "[`~!@#$%^&*()+=|{}:;\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？']";
        Matcher matcher = Pattern.compile(volidPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户不能包含特殊字符");
        }
    }

    private void validateUserAccountDuplicate(String userAccount) {
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = usersMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户重复");
        }
    }

    private void validatePasswordMatch(String password, String checkPassword) {
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和校验密码不匹配");
        }
    }

    private String encryptPassword(String userPassword) {
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    private Users createUser(String nickname,String username, String userAccount, String encryptPassword) {
        Users user = new Users();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        return user;
    }


    @Override
    public Users userLogin(UserLoginRequest loginRequest, HttpServletRequest request) {
        String userAccount = loginRequest.getUserAccount();
        String userPassword = loginRequest.getUserPassword();
        // 1. 校验合法性
        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        validateParameterLength(userAccount,userPassword);
        validateUserAccountSpecialCharacters(userAccount);

        // 2. 校验密码
        String encryptPassword = encryptPassword(userPassword);

        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        Users user = usersMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户登录失败,账户或密码错误");
        }

        // 3. 用户脱敏
        Users safetyUser = getSafetyUser(user);

        // 4. 记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        // 5. 返回脱敏后的用户信息
        return safetyUser;
    }

    @Override
    public Long userRegister(UserRegisterRequest registerRequest) {
        String userAccount = registerRequest.getUserAccount();
        String username = registerRequest.getUsername();
        String userPassword = registerRequest.getUserPassword();
        String checkPassword = registerRequest.getCheckPassword();
        // 1. 校验合法性
        if (StringUtils.isAnyBlank(username,userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        validateParameterLength(userAccount,userPassword);
        validateUserAccountSpecialCharacters(userAccount);
        validateUserAccountDuplicate(userAccount);
        validatePasswordMatch(userPassword,checkPassword);

        // 2. 密码加密
        String encryptPassword = encryptPassword(userPassword);

        // 3.存入数据库
        // 3.1 随机生成昵称
        String nickname = NICKNAME_PREFIX + UUID.randomUUID().toString().substring(0,NICKNAME_LENGTH);
        Users user = createUser(nickname,username,userAccount,encryptPassword);
        boolean result = this.save(user);
        if (!result) {
            throw new RuntimeException("无法插入数据");
        }

        // 4. 返回注册成功的用户ID
        return user.getId();
    }

    @Override
    public Users getCurrentUser(Users currentObj) {
        if (currentObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long userId = currentObj.getId();
        Users user = getById(userId);
        return getSafetyUser(user);
    }

    @Override
    public List<Users> searchUsers(String username) {
        // 1. 查询用户信息
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            // like是 %column%
            queryWrapper.like("username", username);
        }

        // 2. 返回脱敏后的用户信息
        List<Users> userList = list(queryWrapper);
        List<Users> list = userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
//        List<Users> list = userList.stream().map(user -> getSafetyUser(user)).collect(Collectors.toList());

        return list;
    }


    @Override
    public Users getSafetyUser(Users originUser) {
        if (originUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        Users safetyUser = new Users();
        safetyUser.setId(originUser.getId());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setNickname(originUser.getNickname());
        safetyUser.setUserPassword(null);
        safetyUser.setAvatar(originUser.getAvatar());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setGrade(originUser.getGrade());
        safetyUser.setCollege(originUser.getCollege());
        safetyUser.setProfession(originUser.getProfession());
        safetyUser.setHobby(originUser.getHobby());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }

}




