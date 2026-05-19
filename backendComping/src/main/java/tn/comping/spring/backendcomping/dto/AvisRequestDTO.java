package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.TypeCible;

@NoArgsConstructor
@AllArgsConstructor
public class AvisRequestDTO {

    private int note;
    private String commentaire;
    private String cibleId;
    private TypeCible typeCible;
    private String parentAvisId;

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public String getCibleId() { return cibleId; }
    public void setCibleId(String cibleId) { this.cibleId = cibleId; }
    public TypeCible getTypeCible() { return typeCible; }
    public void setTypeCible(TypeCible typeCible) { this.typeCible = typeCible; }
    public String getParentAvisId() { return parentAvisId; }
    public void setParentAvisId(String parentAvisId) { this.parentAvisId = parentAvisId; }
}
