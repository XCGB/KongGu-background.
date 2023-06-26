package com.hongjie.konggu.model.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author: WHJ
 * @createTime: 2023-06-26 14:33
 * @description: 脱敏用户实体类
 */
@Data
public class UserDTO {

    private Long id;

    private String userAccount;

    private String username;

    private String nickname;

    private String avatar;

    private Integer gender;

    private String grade;

    private String college;

    private String profession;

    private String hobby;

    private Integer userStatus;

    private Integer userRole;

    private Date createTime;
}
