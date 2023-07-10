package com.easterfg.takeaway.controller;

import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.utils.ErrorCode;
import com.easterfg.takeaway.utils.ImageUtils;
// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author EasterFG on 2022/9/28
 */
@RestController
@RequestMapping("/common")
@Slf4j
// @Api(tags = "通用工具接口")
public class CommonController {

    @Value("${upload.path}")
    private String uploadPath;

    /**
     * 文件上传
     */
//    @Authorize(Role.EMPLOYEE)
    // @ApiOperation("文件上传")
    @PostMapping("upload")
    public Result uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null) {
            return Result.failed(ErrorCode.USER_REQUEST_PARAMETER_ERROR);
        }
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return Result.failed(ErrorCode.USER_REQUEST_PARAMETER_ERROR);
        } else if (!ImageUtils.isImage(file)) {
            return Result.failed(ErrorCode.ILLEGAL_FILE);
        }
        String prefix = filename.substring(filename.indexOf('.'));
        String fn = UUID.randomUUID().toString().replace("-", "") + prefix;
        // 验证文件合法性
        File path = new File(uploadPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        File dest = new File(path, fn);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error(e.toString(), e);
            return Result.failed(ErrorCode.FILE_UPLOAD_FAILED);
        }
        // 封装图片名称返回
        return Result.success("上传成功", fn);
    }

    /**
     * 通过图片名称获取图片
     */
    // @ApiOperation("文件下载")
    @GetMapping(value = "/download/{name}",
            produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BufferedImage> downloadImage(@PathVariable String name) throws IOException {
        if (!name.matches("[a-zA-Z0-9.]+")) {
            return ResponseEntity
                    .badRequest()
                    .body(null);
        }
        FileInputStream ins = new FileInputStream(new File(uploadPath, name));
        MediaType contentType = MediaType.IMAGE_JPEG;
        if (name.endsWith(".png")) {
            contentType = MediaType.IMAGE_PNG;
        }
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(1800, TimeUnit.SECONDS))
                .contentType(contentType)
                .eTag(name)
                .body(ImageIO.read(ins));
    }
}
