package tn.comping.spring.backendcomping.services.serviceImpl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);
    private final Cloudinary cloudinary;

    /**
     * Upload une image et retourne l'URL sécurisée HTTPS (secure_url).
     * Utilisé pour les sorties, équipes, et photos de profil.
     */
    public String uploadImage(MultipartFile file) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder",   "campino",        // dossier dans Cloudinary
                        "use_filename", true,
                        "unique_filename", true
                )
        );
        // secure_url = HTTPS, url = HTTP
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Supprime une image par son public_id Cloudinary.
     * Appeler lors de la suppression d'une sortie ou équipe.
     */
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Image supprimée de Cloudinary : {}", publicId);
        } catch (IOException e) {
            log.warn("Impossible de supprimer l'image Cloudinary : {}", publicId);
        }
    }
}