package tn.comping.spring.backendcomping.entities;

public enum statutProduit {
    Disponible,
    STOCK_FAIBLE,      // stock <= seuil d’alerte
    RUPTURE_STOCK      // stock = 0
}
