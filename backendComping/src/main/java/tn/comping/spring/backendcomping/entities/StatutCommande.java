package tn.comping.spring.backendcomping.entities;

public enum StatutCommande {
    EN_ATTENTE,        // order created
    CONFIRMEE,         // validated by user
    EN_PREPARATION,    // preparing order
    EXPEDIEE,          // shipped
    LIVREE,            // delivered
    ANNULEE            // cancelled
}