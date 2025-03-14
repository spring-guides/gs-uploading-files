package com.example.uploadingfiles; // Ensure the package matches

import com.azure.storage.blob.BlobClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AzureBlobStorageService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.blob-container}")
    private String containerName;

    public void uploadFile(MultipartFile file) {
        var blobClient = new BlobClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .blobName(file.getOriginalFilename())
                .buildClient();

        blobClient.upload(file.getInputStream(), file.getSize(), true);
    }
}
