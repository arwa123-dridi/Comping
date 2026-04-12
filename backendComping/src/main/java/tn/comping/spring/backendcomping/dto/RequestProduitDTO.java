package tn.comping.spring.backendcomping.dto;
import tn.comping.spring.backendcomping.entities.categorieProduit;
import tn.comping.spring.backendcomping.entities.statutProduit;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestProduitDTO {
  private String nomProduit; 
  private String descriptionProduit; 
  private Double prixProduit; 
  private categorieProduit categorieProduit; 
  private String typeProduit; 
  private statutProduit  statut;

}
