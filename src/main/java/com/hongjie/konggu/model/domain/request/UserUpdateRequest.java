package com.hongjie.konggu.model.domain.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: WHJ
 * @createTime: 2023-06-20 11:37
 * @description: 用户更新类
 */
@Data
public class UserUpdateRequest implements Serializable {
    private static final long serialVersionUID = -254514408772907139L;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别 0-男 1-女
     */
    private Integer gender;

    /**
     * 入学年级
     */
    private String grade;

    /**
     * 学院
     */
    private String college;

    /**
     * 专业
     */
    private String profession;

    /**
     * 爱好
     */
    private String hobby;
}
