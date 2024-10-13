package com.example.uploadingfiles;

import java.io.IOException;
import java.util.stream.Collectors;

import com.example.upload.AzureBlobStorageService; // Ensure this path is correct

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.uploadingfiles.storage.StorageFileNotFoundException;

@Controller
public class FileUploadController {

    // Removed StorageService and added AzureBlobStorageService
    private final AzureBlobStorageService azureBlobStorageService;

    @Autowired
    public FileUploadController(AzureBlobStorageService azureBlobStorageService) {
        this.azureBlobStorageService = azureBlobStorageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {
        // Note: You may want to modify this method if you want to list files from Azure Blob Storage
        // For now, we can keep it unchanged, but remember Azure Blob Storage requires different handling.
        model.addAttribute("files", 
                azureBlobStorageService.getAllBlobs().stream() // Implement this method in your service
                .map(blobName -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", blobName).build().toUri().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = azureBlobStorageService.loadAsResource(filename); // Implement this method in your service

        if (file == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        azureBlobStorageService.uploadFile(file); // Use Azure service for upload
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}
