package com.hongjie.konggu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongjie.konggu.common.ErrorCode;
import com.hongjie.konggu.exception.BusinessException;
import com.hongjie.konggu.model.domain.Users;
import com.hongjie.konggu.model.dto.UserDTO;
import com.hongjie.konggu.model.request.UserAppendRequest;
import com.hongjie.konggu.model.request.UserLoginRequest;
import com.hongjie.konggu.model.request.UserRegisterRequest;
import com.hongjie.konggu.model.request.UserUpdateRequest;
import com.hongjie.konggu.service.UsersService;
import com.hongjie.konggu.mapper.UsersMapper;
import com.hongjie.konggu.utils.CacheClient;
import com.hongjie.konggu.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hongjie.konggu.constant.RedisConstants.LOGIN_USER_KEY;
import static com.hongjie.konggu.constant.RedisConstants.LOGIN_USER_TTL;
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
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CacheClient cacheClient;

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
        user.setAvatar(AVATAR_URL);
        return user;
    }


    @Override
    public String userLogin(UserLoginRequest loginRequest, HttpSession session) {
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

        // 4. 保存用户到Redis
        // 4.1 生成Token作为 key
        String token = cn.hutool.core.lang.UUID.randomUUID().toString(false);
        String tokenKey = LOGIN_USER_KEY + token;
        // 4.2 将User对象转为Hash
        UserDTO userDTO = BeanUtil.copyProperties(safetyUser, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true) //忽略源对象中的空值属性，只复制非空值的属性
                        .setFieldValueEditor((fieldName, fieldValue) ->
                                fieldValue != null ? fieldValue.toString() : null) // 将字段的值转换为字符串类型
        );

        stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 5. 返回脱敏后的用户信息
        return token;
    }

    @Override
    public Boolean userLogout(HttpServletRequest request) {
        //1. 从header中获取token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)){
            return false;
        }

        //2. 从redis中获取用户
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.delete(tokenKey);
        return true;
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
    public UserDTO getLoginUser(HttpServletRequest request) {
        // 1. 从线程中获取用户
        UserDTO user = UserHolder.getUser();
        // 2. 放行
        return user;

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
    public Long appendUser(UserAppendRequest userAppendRequest) {
        String userAccount = userAppendRequest.getUserAccount();
        String userPassword = "12345678";

        // 1. 校验合法性
        if (StringUtils.isAnyBlank(userAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        validateUserAccountSpecialCharacters(userAccount);
        validateUserAccountDuplicate(userAccount);
        validateParameterLength(userAccount,userPassword);

        // 2. 获取用户信息
        String avatarUrl = userAppendRequest.getAvatar();
        String username = userAppendRequest.getUsername();
        String nickname = NICKNAME_PREFIX + UUID.randomUUID().toString().substring(0,NICKNAME_LENGTH);
        String encryptPassword = encryptPassword(userPassword);

        // 3. 插入数据
        Users user = new Users();
        user.setUsername(username);
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setNickname(nickname);
        user.setAvatar(avatarUrl);
        boolean result = this.save(user);

        // 4. 返回插入成功的用户ID
        if (!result) {
            throw new BusinessException(ErrorCode.INSERT_ERROR, "无法插入数据");
        }
        return user.getId();
    }

    @Override
    public Boolean updateUser(Long id, UserUpdateRequest updateUser, HttpServletRequest request) {
        //1. 从header中获取token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)){
            return null;
        }

        //2. 从redis中获取用户
        String tokenKey = LOGIN_USER_KEY + token;
        Map<Object, Object> cacheUser = stringRedisTemplate.opsForHash().entries(tokenKey);

        // 3. 判断用户是否存在
        if (cacheUser.isEmpty()){
            return null;
        }

        // 4. 将HashMap类型的User转换成UserDTO
        UserDTO userDTO = BeanUtil.fillBeanWithMap(cacheUser, new UserDTO(), false);

        Users user = usersMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户为空");
        }

        // 更新用户属性
        if (updateUser.getNickname() != null) {
            user.setNickname(updateUser.getNickname());
            userDTO.setNickname(updateUser.getNickname());
        }
        if (updateUser.getAvatar() != null) {
            user.setAvatar(updateUser.getAvatar());
            userDTO.setAvatar(updateUser.getAvatar());
        }
        if (updateUser.getGender() != null) {
            user.setGender(updateUser.getGender());
            userDTO.setGender(updateUser.getGender());
        }
        if (updateUser.getGrade() != null) {
            user.setGrade(updateUser.getGrade());
            userDTO.setGrade(updateUser.getGrade());
        }
        if (updateUser.getCollege() != null) {
            user.setCollege(updateUser.getCollege());
            userDTO.setCollege(updateUser.getCollege());
        }
        if (updateUser.getProfession() != null) {
            user.setProfession(updateUser.getProfession());
            userDTO.setProfession(updateUser.getProfession());
        }
        if (updateUser.getHobby() != null) {
            user.setHobby(updateUser.getHobby());
            userDTO.setHobby(updateUser.getHobby());
        }

        // 保存更新后的用户信息
        int rows = usersMapper.updateById(user);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true) //忽略源对象中的空值属性，只复制非空值的属性
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()) //将字段的值转换为字符串类型。
        );
        stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);

        if (rows > 0) {
            return true;
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户为空");
        }

    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        UserDTO loginUser = getLoginUser(request);
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
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




