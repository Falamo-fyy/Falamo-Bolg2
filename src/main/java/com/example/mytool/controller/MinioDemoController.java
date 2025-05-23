package com.example.mytool.controller;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.UUID;

/**
 * MinIO演示控制器
 * 用于演示MinIO对象存储的基本操作，包括文件上传
 */

@Controller
public class MinioDemoController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(MinioDemoController.class);

    private final MinioClient minioClient;

    @Autowired
    public MinioDemoController(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Value("${minio.bucket-name}")
    private String bucketName;

    @GetMapping("/minio-profile")
    public String profilePage(Model model) {
        // 可以添加需要的用户数据到model
        return "profile"; // 对应templates/profile.html
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            // 检查存储桶是否存在
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(UUID.randomUUID() + "_" + file.getOriginalFilename()) // 添加唯一前缀
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            redirectAttributes.addFlashAttribute("success", "文件上传成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "上传失败: " + e.getMessage());
            logger.error("MinIO上传错误", e);
        }
        return "redirect:/user/profile";
    }

}