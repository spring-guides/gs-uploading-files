package com.example.uploadingfiles.storage

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("storage")
class StorageProperties {
    /**
     * Folder location for storing files
     */
    var location: String = "upload-dir"
}
