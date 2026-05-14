package tn.comping.spring.backendcomping.entities;

public enum statutProduit {
    DISPONIBLE,
    STOCK_FAIBLE,      // stock <= seuil d’alerte
    RUPTURE_STOCK      // stock = 0
}
