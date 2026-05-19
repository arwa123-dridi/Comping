package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import tn.comping.spring.backendcomping.dto.RequestProduitDTO;
import tn.comping.spring.backendcomping.dto.ResponseProduitDTO;
import tn.comping.spring.backendcomping.entities.Produit;
import tn.comping.spring.backendcomping.entities.statutProduit;
import tn.comping.spring.backendcomping.repositories.ProduitRepository;
import tn.comping.spring.backendcomping.utils.mapper.ProduitMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProduitServiceImpl implements ProduitInter {

    private final ProduitRepository produitRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public ResponseProduitDTO addProduit(RequestProduitDTO produitDTO, MultipartFile image) {
        Produit produit = ProduitMapper.toEntity(produitDTO);

        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(image);
                produit.setImageUrl(imageUrl);
            } catch (IOException e) {
                log.error("Failed to upload image to Cloudinary: {}", e.getMessage());
            }
        }

        updateStatutProduit(produit);
        Produit savedProduit = produitRepository.save(produit);

        boolean promoActive = isPromoActive(savedProduit);
        double finalPrice = calculateFinalPrice(savedProduit);

        return ProduitMapper.toResponseDTO(savedProduit, finalPrice, promoActive);
    }

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

    @Override
    public Page<ResponseProduitDTO> getPagedProduits(Pageable pageable, String category) {
        // Simple implementation for now, filtering by category can be added to repository
        return produitRepository.findAll(pageable)
                .map(produit -> {
                    boolean promoActive = isPromoActive(produit);
                    double finalPrice = calculateFinalPrice(produit);
                    return ProduitMapper.toResponseDTO(produit, finalPrice, promoActive);
                });
    }

    @Override
    public ResponseProduitDTO getProduitById(String id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit not found with id: " + id));
        boolean promoActive = isPromoActive(produit);
        double finalPrice = calculateFinalPrice(produit);

        return ProduitMapper.toResponseDTO(produit, finalPrice, promoActive);
    }

    @Override
    public ResponseProduitDTO updateProduit(String id, RequestProduitDTO produitDTO, MultipartFile image) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit not found"));

        ProduitMapper.updateEntityFromDTO(produitDTO, produit);

        if (image != null && !image.isEmpty()) {
            try {
                String newImageUrl = cloudinaryService.uploadImage(image);
                produit.setImageUrl(newImageUrl);
            } catch (IOException e) {
                log.error("Failed to upload image to Cloudinary: {}", e.getMessage());
            }
        }

        updateStatutProduit(produit);
        Produit updatedProduit = produitRepository.save(produit);

        boolean promoActive = isPromoActive(updatedProduit);
        double finalPrice = calculateFinalPrice(updatedProduit);

        return ProduitMapper.toResponseDTO(updatedProduit, finalPrice, promoActive);
    }

    @Override
    public String deleteProduit(String id) {
        produitRepository.deleteById(id);
        return "Produit deleted successfully";
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

    @Override
    public Double calculateFinalPrice(Produit produit) {
        if (isPromoActive(produit) && produit.getPromoPrice() != null) {
            return produit.getPromoPrice();
        }
        return produit.getPrixProduit();
    }

    private boolean isPromoActive(Produit produit) {
        if (produit.getPromoPrice() == null || produit.getPromoStart() == null || produit.getPromoEnd() == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(produit.getPromoStart()) && now.isBefore(produit.getPromoEnd());
    }

    private void updateStatutProduit(Produit produit) {
        if (produit.getQuantiteStock() == null || produit.getQuantiteStock() <= 0) {
            produit.setStatut(statutProduit.RUPTURE_STOCK);
        } else if (produit.getSeuilAlerteStock() != null && produit.getQuantiteStock() <= produit.getSeuilAlerteStock()) {
            produit.setStatut(statutProduit.STOCK_FAIBLE);
        } else {
            produit.setStatut(statutProduit.EN_STOCK);
        }
    }
}
