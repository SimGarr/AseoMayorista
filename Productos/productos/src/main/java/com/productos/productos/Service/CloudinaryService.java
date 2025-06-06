package com.productos.productos.Service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "Root",
            "api_key", "419633398653776",
            "api_secret", "KWXbeoJQckebDtXmyqHCpU46yR0",
            "secure", true
        ));
    }

    public String subirImagen(MultipartFile archivo) throws IOException {
        Map<?, ?> resultado = cloudinary.uploader().upload(archivo.getBytes(), ObjectUtils.emptyMap());
        return resultado.get("secure_url").toString();
    }
}
