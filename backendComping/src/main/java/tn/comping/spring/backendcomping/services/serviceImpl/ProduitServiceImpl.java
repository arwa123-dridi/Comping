package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import tn.comping.spring.backendcomping.dto.RequestProduitDTO;
import tn.comping.spring.backendcomping.dto.ResponseProduitDTO;
import tn.comping.spring.backendcomping.entities.Produit;
import tn.comping.spring.backendcomping.repositories.ProduitRepository;
import tn.comping.spring.backendcomping.utils.mapper.ProduitMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProduitServiceImpl implements ProduitInter {

    private final ProduitRepository produitRepository;

    // ⭐ upload folder
    private final String UPLOAD_DIR = "uploads/products/";

    // ================= ADD PRODUIT WITH IMAGE =================
    @Override
    public ResponseProduitDTO addProduit(RequestProduitDTO produitDTO, MultipartFile image) {

        Produit produit = ProduitMapper.toEntity(produitDTO);

        // upload image if exists
        if (image != null && !image.isEmpty()) {
            String imageUrl = uploadImage(image);
            produit.setImageUrl(imageUrl);
        }

        Produit savedProduit = produitRepository.save(produit);
        return ProduitMapper.toResponseDTO(savedProduit);
    }

    // ================= GET ALL =================
    @Override
    public List<ResponseProduitDTO> getAllProduits() {
        return produitRepository.findAll()
                .stream()
                .map(ProduitMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ================= GET BY ID =================
    @Override
    public ResponseProduitDTO getProduitById(String id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit not found with id: " + id));
        return ProduitMapper.toResponseDTO(produit);
    }

    // ================= UPDATE WITH IMAGE =================
    @Override
    public ResponseProduitDTO updateProduit(String id, RequestProduitDTO produitDTO, MultipartFile image) {

        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit not found"));

        ProduitMapper.updateEntityFromDTO(produitDTO, produit);

        // replace image if new uploaded
        if (image != null && !image.isEmpty()) {
            String imageUrl = uploadImage(image);
            produit.setImageUrl(imageUrl);
        }

        Produit updatedProduit = produitRepository.save(produit);
        return ProduitMapper.toResponseDTO(updatedProduit);
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
                .map(ProduitMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}