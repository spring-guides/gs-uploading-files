package com.example.uploadingfiles.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("storage")
class StorageProperties {
    /**
     * Folder location for storing files
     */
    var location: String = "upload-dir"
}
