package com.example.uploadingfiles.storage

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

@Service
class FileSystemStorageService @Autowired constructor(properties: StorageProperties) : StorageService {

    private val rootLocation: Path

    init {
        if (properties.location.trim().isEmpty()) {
            throw StorageException("File upload location can not be Empty.")
        }
        this.rootLocation = Paths.get(properties.location)
    }

    override fun store(file: MultipartFile) {
        try {
            if (file.isEmpty) {
                throw StorageException("Failed to store empty file ${file.originalFilename}")
            }
            Files.copy(file.inputStream, this.rootLocation.resolve(file.originalFilename!!))
        } catch (e: IOException) {
            throw StorageException("Failed to store file ${file.originalFilename}", e)
        }
    }

    override fun loadAll(): Stream<Path> {
        try {
            Files.walk(this.rootLocation, 1).use { paths ->
                return paths
                    .filter { path -> path != this.rootLocation }
                    .map { path -> this.rootLocation.relativize(path) }
                    .toList()
                    .stream()
            }
        } catch (e: IOException) {
            throw StorageException("Failed to read stored files", e)
        }
    }

    override fun load(filename: String): Path = rootLocation.resolve(filename)

    override fun loadAsResource(filename: String): Resource {
        try {
            val file = load(filename)
            val resource: Resource = UrlResource(file.toUri())
            return if (resource.exists() || resource.isReadable) {
                resource
            } else {
                throw StorageFileNotFoundException("Could not read file: $filename")
            }
        } catch (e: MalformedURLException) {
            throw StorageFileNotFoundException("Could not read file: $filename", e)
        }
    }

    override fun deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile())
    }

    override fun init() {
        try {
            Files.createDirectory(rootLocation)
        } catch (e: IOException) {
            throw StorageException("Could not initialize storage", e)
        }
    }
}
