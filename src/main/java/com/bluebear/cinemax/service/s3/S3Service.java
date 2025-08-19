package com.bluebear.cinemax.service.s3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Paths;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    private final String BUCKET_NAME = "bluebear-bucket";

    public void uploadFile (String keyName, String filePath) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(BUCKET_NAME).key(keyName).build();
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(Paths.get(filePath)));
    }

    public void downloadFile (String keyName, String downloadFlash) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(BUCKET_NAME).key(keyName).build();
        s3Client.getObject(getObjectRequest, Paths.get(downloadFlash));
    }

}
