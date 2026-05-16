package tn.comping.spring.backendcomping.entities;

public enum StatutPaiement {
    EN_ATTENTE,
    PAYE,
    ANNULE,
    REMBOURSE;

    public StatutPaiement valider() {
        if (this == EN_ATTENTE) return PAYE;
        throw new IllegalStateException("Impossible de valider un paiement avec le statut : " + this);
    }

    public StatutPaiement rembourser() {
        if (this == PAYE) return REMBOURSE;
        throw new IllegalStateException("Impossible de rembourser un paiement avec le statut : " + this);
    }
}