package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import tn.comping.spring.backendcomping.dto.RequestProduitDTO;
import tn.comping.spring.backendcomping.dto.ResponseProduitDTO;
import tn.comping.spring.backendcomping.entities.Produit;
import tn.comping.spring.backendcomping.entities.statutProduit;
import tn.comping.spring.backendcomping.repositories.ProduitRepository;
import tn.comping.spring.backendcomping.utils.mapper.ProduitMapper;
import tn.comping.spring.backendcomping.services.serviceImpl.EmailServiceProduct;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProduitServiceImpl implements ProduitInter {

    private final ProduitRepository produitRepository;
    private final EmailServiceProduct emailService;

    // ⭐ upload folder
    private final String UPLOAD_DIR = "uploads/products/";

    // ================= ADD PRODUIT WITH IMAGE =================
    @Override
    public ResponseProduitDTO addProduit(RequestProduitDTO produitDTO, MultipartFile image) {

        Produit produit = ProduitMapper.toEntity(produitDTO);

        // upload image
        if (image != null && !image.isEmpty()) {
            String imageUrl = uploadImage(image);
            produit.setImageUrl(imageUrl);
        }

        // 🆕 calculate stock status BEFORE saving
        updateStatutProduit(produit);

        Produit savedProduit = produitRepository.save(produit);

        boolean promoActive = isPromoActive(savedProduit);
        double finalPrice = calculateFinalPrice(savedProduit);

        return ProduitMapper.toResponseDTO(savedProduit, finalPrice, promoActive);
    }

    // ================= GET ALL =================
    @Override
    public List<ResponseProduitDTO> getAllProduits() {
        return produitRepository.findAll()
                .stream()
                .map(produit -> {
                    boolean promoActive = isPromoActive(produit);
                    double finalPrice = calculateFinalPrice(produit);
                    return ProduitMapper.toResponseDTO(produit, finalPrice, promoActive);
                })
                .collect(Collectors.toList());
    }

    // ================= GET BY ID =================
    @Override
    public ResponseProduitDTO getProduitById(String id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit not found with id: " + id));
        boolean promoActive = isPromoActive(produit);
        double finalPrice = calculateFinalPrice(produit);

        return ProduitMapper.toResponseDTO(produit, finalPrice, promoActive);
    }

    // ================= UPDATE WITH IMAGE =================
    @Override
    public ResponseProduitDTO updateProduit(String id, RequestProduitDTO produitDTO, MultipartFile image) {

        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit not found"));

        // ⭐ STEP 1 — SAVE CURRENT IMAGE BEFORE MAPPING
        String existingImageUrl = produit.getImageUrl();

        // ⭐ STEP 2 — UPDATE FIELDS FROM DTO
        ProduitMapper.updateEntityFromDTO(produitDTO, produit);

        // ⭐ STEP 3 — HANDLE IMAGE PROPERLY
        if (image != null && !image.isEmpty()) {
            // Upload new image
            String newImageUrl = uploadImage(image);

            // Delete old image from disk (PRO feature)
            deleteOldImage(existingImageUrl);

            produit.setImageUrl(newImageUrl);
        } else {
            // Keep existing image if no new image uploaded
            produit.setImageUrl(existingImageUrl);
        }

        // ⭐ STEP 4 — RECALCULATE STOCK STATUS
        updateStatutProduit(produit);

        // ⭐ STEP 5 — SAVE
        Produit updatedProduit = produitRepository.save(produit);

        boolean promoActive = isPromoActive(updatedProduit);
        double finalPrice = calculateFinalPrice(updatedProduit);

        return ProduitMapper.toResponseDTO(updatedProduit, finalPrice, promoActive);
    }

    private void deleteOldImage(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isEmpty())
                return;

            Path oldImagePath = Paths.get("." + imageUrl);
            Files.deleteIfExists(oldImagePath);

        } catch (IOException e) {
            System.out.println("Could not delete old image: " + e.getMessage());
        }
    }

    // ================= DELETE =================
    @Override
    public String deleteProduit(String id) {
        produitRepository.deleteById(id);
        return "Produit deleted successfully";
    }

    // ================= IMAGE UPLOAD METHOD =================
    private String uploadImage(MultipartFile file) {
        try {
            File folder = new File(UPLOAD_DIR);
            if (!folder.exists())
                folder.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);

            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/products/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Error uploading image");
        }
    }

    @Override
    public List<ResponseProduitDTO> searchProduitsByName(String nomProduit) {
        return produitRepository.findByNomProduitContainingIgnoreCase(nomProduit)
                .stream()
                .map(produit -> {
                    boolean promoActive = isPromoActive(produit);
                    double finalPrice = calculateFinalPrice(produit);
                    return ProduitMapper.toResponseDTO(produit, finalPrice, promoActive);
                })
                .collect(Collectors.toList());
    }

    // updatestatutProduit
    public void updateStatutProduit(Produit produit) {

        if (produit.getQuantiteStock() == null)
            return;

        statutProduit ancienStatut = produit.getStatut();

        if (produit.getQuantiteStock() == 0) {
            produit.setStatut(statutProduit.RUPTURE_STOCK);

            // Send email ONLY when product becomes out of stock
            if (ancienStatut != statutProduit.RUPTURE_STOCK) {
                emailService.sendOutOfStockEmail(produit);
            }

        } else if (produit.getQuantiteStock() <= produit.getSeuilAlerteStock()) {
            produit.setStatut(statutProduit.STOCK_FAIBLE);

        } else {
            produit.setStatut(statutProduit.DISPONIBLE);
        }
    }

    public Double calculateFinalPrice(Produit produit) {

        LocalDateTime now = LocalDateTime.now();

        boolean promoActive = produit.getPromoPrice() != null &&
                produit.getPromoStart() != null &&
                produit.getPromoEnd() != null &&
                now.isAfter(produit.getPromoStart()) &&
                now.isBefore(produit.getPromoEnd());

        if (promoActive) {
            return produit.getPromoPrice();
        }

        return produit.getPrixProduit();
    }

    private boolean isPromoActive(Produit produit) {
        LocalDateTime now = LocalDateTime.now();

        return produit.getPromoPrice() != null &&
                produit.getPromoStart() != null &&
                produit.getPromoEnd() != null &&
                now.isAfter(produit.getPromoStart()) &&
                now.isBefore(produit.getPromoEnd());
    }

}