-- 创建库
create database if not exists kong_gu;

-- 切换库
use kong_gu;

-- 用户表
create table users
(
    id           bigint auto_increment comment '用户ID'
        primary key,
    userAccount  varchar(15)                        not null comment '学号（全局唯一）',
    username     varchar(50)                        not null comment '用户姓名',
    nickname     varchar(50)                        null comment '昵称',
    userPassword varchar(128)                       not null comment '密码（加密存储）',
    avatar       varchar(256)                       null comment '头像',
    gender       tinyint  default 0                 not null comment '性别 0-男 1-女',
    grade        varchar(256)                       null comment '入学年级',
    college      varchar(256)                       null comment '学院',
    profession   varchar(256)                       null comment '专业',
    hobby        varchar(256)                       null comment '爱好',
    userStatus   tinyint  default 0                 null comment '用户状态 0-正常 1-封号',
    userRole     tinyint  default 0                 null comment '用户鉴权 0-普通用户 1-高级用户 2-管理用户',
    isDelete     tinyint  default 0                 null comment '逻辑删除 0-存在 1-删除',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint users_uq
        unique (userAccount)
) comment '用户表';


-- 帖子表
create table post
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    userId        bigint                             not null comment '发帖用户ID',
    content       text                               not null comment '发帖内容',
    reviewStatus  tinyint  default 0                 not null comment '审核状态 0-待审核 1-通过 2-拒绝',
    reviewMessage varchar(512)                       null comment '审核信息',
    thumNum       int      default 0                 not null comment '点赞数',
    commentNum    int      default 0                 not null comment '评论数',
    isDelete      tinyint  default 0                 not null comment '逻辑删除 ',
    createTime    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '帖子表';

-- 帖子点赞表
create table post_thumb
(
    id         bigint auto_increment comment '主键ID'
        primary key,
    userId     bigint                             not null comment '点赞用户ID',
    postId     bigint                             not null comment '帖子ID',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '帖子点赞表';

