package tn.comping.spring.backendcomping.utils;

public class Constants {

    public static final String APP_ROOT = "/api";
    public static final String AUTH = "/auth";

    // ================= EVENT =================
    public static final String BASE_URL_EVENT = "/api/events";
    public static final String CREATE_EVENT = "/CREATE/EVENT";
    public static final String GET_ALL_EVENTS = "";
    public static final String GET_EVENT_BY_ID = "/EVENTBYID/{id}";
    public static final String UPDATE_EVENT = "/UPDATE/{id}";
    public static final String DELETE_EVENT = "/{id}";
    public static final String COUNT_VALID = "/count/valide";
    public static final String COUNT_DONE = "/count/termine";
    public static final String COUNT_CANCELLED = "/count/annule";

    // ================= PARTICIPATION =================
    public static final String PARTICIPATE = "/{eventId}/participate";
    public static final String CANCEL_PARTICIPATION = "/{eventId}/cancel";

    // ================= ACTIVITY =================
    public static final String BASE_URL_ACTIVITY = "/api/activities";
    public static final String CREATE_ACTIVITY = "/add";
    public static final String GET_ALL_ACTIVITIES = "/GetAllActivities";
    public static final String GET_ACTIVITY_BY_ID = "/{id}";
    public static final String UPDATE_ACTIVITY = "/updateactivity/{id}";
    public static final String DELETE_ACTIVITY = "/deleteactivity/{id}";

    // ================= DEMANDE TRANSPORT =================
    public static final String BASE_URL_DEMANDE_TRANSPORT = "/api/demandes-transport";
    public static final String CREATE_DEMANDE_TRANSPORT = "";
    public static final String GET_ALL_DEMANDES_TRANSPORT = "";
    public static final String GET_DEMANDE_TRANSPORT_BY_ID = "/{id}";
    public static final String UPDATE_DEMANDE_TRANSPORT = "/{id}";
    public static final String DELETE_DEMANDE_TRANSPORT = "/{id}";

    // ================= USERS =================
    public static final String GET_ALL_USERS = "";
    public static final String DELETE_USER = "/{userId}";

    // ================= CRENEAU LIVRAISON =================
    public static final String BASE_URL_CRENEAU_LIVRAISON = "/api/creneaux-livraison";
    public static final String CREATE_CRENEAU_LIVRAISON = "";
    public static final String GET_ALL_CRENEAUX_LIVRAISON = "";
    public static final String GET_CRENEAU_LIVRAISON_BY_ID = "/{id}";
    public static final String UPDATE_CRENEAU_LIVRAISON = "/{id}";
    public static final String DELETE_CRENEAU_LIVRAISON = "/{id}";

    // ================= INCIDENT =================
    public static final String BASE_URL_INCIDENT = "/api/incidents";
    public static final String CREATE_INCIDENT = "";
    public static final String GET_ALL_INCIDENTS = "";
    public static final String GET_INCIDENT_BY_ID = "/{id}";
    public static final String UPDATE_INCIDENT = "/{id}";
    public static final String DELETE_INCIDENT = "/{id}";

    // ================= CONVENTIONS PARTENAIRES =================
    public static final String BASE_URL_CONVENTION_PARTENAIRE = "/api/conventions-partenaires";
    public static final String CREATE_CONVENTION_PARTENAIRE = "";
    public static final String GET_ALL_CONVENTIONS_PARTENAIRES = "";
    public static final String GET_CONVENTION_PARTENAIRE_BY_ID = "/{id}";
    public static final String UPDATE_CONVENTION_PARTENAIRE = "/{id}";
    public static final String DELETE_CONVENTION_PARTENAIRE = "/{id}";

    // ================= PRODUIT =================
    public static final String BASE_URL_PRODUIT = "/api/produits";
    public static final String CREATE_PRODUIT = "/addProduct";
    public static final String GET_ALL_PRODUITS = "/allProduct";
    public static final String UPDATE_PRODUIT = "/updateProduct/{id}";
    public static final String DELETE_PRODUIT = "/deleteProduct/{id}";
}