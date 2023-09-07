package com.hongjie.konggu.controller;

import com.hongjie.konggu.common.BaseResponse;
import com.hongjie.konggu.common.ResultUtil;
import com.hongjie.konggu.model.domain.Tag;
import com.hongjie.konggu.utils.AliOSSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author: WHJ
 * @createTime: 2023-06-20 14:19
 * @description: 上传文件控制层
 */
@RequestMapping("/upload")
@Api(tags = "文件更新控制器")
@RestController
@Slf4j
public class UploadController {
    @Resource
    private AliOSSUtils aliOSSUtils;

    /**
     * 更新图片（AliOSS)
     *
     * @param image   图片文件
     * @return {@link BaseResponse}<{@link String}>
     * @throws IOException IOException
     */
    @PostMapping("/avatar")
    @ApiOperation(value = "更新图片")
    public BaseResponse<String> upload(MultipartFile image) throws IOException {
        log.info("文件上传，文件名{}", image.getOriginalFilename());
        String url = aliOSSUtils.upload(image);
        log.info("文件上传成功，文件访问URL：{}", url);
        return ResultUtil.success(url);
    }
}
