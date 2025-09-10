package com.keetlo.banner_management.utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Base64;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class ImageHandler {
    @Value("${image.root.directory}")
    private String FILE_ROOT_DIRECTORY;

    public ImageHandler(){}

    //getter
    public String getFileRootDirectory() {
        return FILE_ROOT_DIRECTORY;
    }

    public void saveBase64Image(String base64Image, String filePath) throws IOException {
        String imageString = base64Image.split(",")[1];  // Remove the data:image/png;base64, part
        String fullFilePath = FILE_ROOT_DIRECTORY + filePath;
        System.out.println(fullFilePath);
        byte[] decodedBytes = Base64.getDecoder().decode(imageString);
        File file = new File(fullFilePath);

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();  // This will create the directory and any missing parent directories
        }

        try (FileOutputStream fos = new FileOutputStream(fullFilePath)) {
            fos.write(decodedBytes);
        }
    }

    public boolean isBase64(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        try {
            String base64String = input.contains(",") ? input.split(",")[1] : input;
            Base64.getDecoder().decode(base64String);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    public boolean deleteImage(String filePath) {
        File file = new File(FILE_ROOT_DIRECTORY+filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
}
