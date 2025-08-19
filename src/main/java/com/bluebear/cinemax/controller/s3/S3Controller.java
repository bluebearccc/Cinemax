package com.bluebear.cinemax.controller.s3;

import com.bluebear.cinemax.service.s3.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/s3")
public class S3Controller {

    @Autowired
    private S3Service s3Service;

    @PostMapping("/upload")
    public String uploadFile (@RequestParam("file") MultipartFile file) throws IOException {
        String keyName = file.getOriginalFilename();
        File tempFile = File.createTempFile("temp", null);
        file.transferTo(tempFile);
        s3Service.uploadFile(keyName, tempFile.getAbsolutePath());
        return "File uploaded successfully !";
    }

    @GetMapping("/download")
    public String downloadFile (@RequestParam("keyName") String keyName, @RequestParam("downloadPath") String downloadPath) throws IOException {
        s3Service.downloadFile(keyName, downloadPath);
        return "File downloaded successfully !";
    }

}
