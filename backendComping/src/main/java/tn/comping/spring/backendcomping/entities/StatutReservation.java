package tn.comping.spring.backendcomping.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum StatutReservation {
    EN_ATTENTE,
    CONFIRME,
    CONFIRMEE,
    ANNULEE,
    TERMINEE;

    @JsonCreator
    public static StatutReservation fromValue(String value) {
        for (StatutReservation s : values()) {
            if (s.name().equalsIgnoreCase(value.trim())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Statut inconnu : " + value);
    }
}