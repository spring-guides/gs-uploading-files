package com.example.uploadingfiles

import com.example.uploadingfiles.storage.StorageProperties
import com.example.uploadingfiles.storage.StorageService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties::class)
class UploadingFilesApplication {

    @Bean
    fun init(storageService: StorageService) = CommandLineRunner {
        storageService.deleteAll()
        storageService.init()
    }
}

fun main(args: Array<String>) {
    runApplication<UploadingFilesApplication>(*args)
}