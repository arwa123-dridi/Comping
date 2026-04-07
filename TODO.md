# TODO: Mise à jour modules Avis/Chat/Social

## Plan approuvé - Étapes à suivre:

### Phase 1: Mises à jour mineures Avis (✅ Complet)
- [x] 1. Ajouter champ `titre` à AvisRequestDTO.java
- [x] 2. Mapper titre+contenu → commentaire dans AvisServiceImpl.java (via AvisMapper)
- [ ] 3. (Optionnel) Ajouter `titre` field à Avis entity si persistance needed

### Phase 2: Module Chat (✅ Complet)
- [ ] 4. Créer entities: Conversation.java, Message.java
- [ ] 5. Créer DTOs: ConversationRequest/ResponseDTO.java, MessageRequest/ResponseDTO.java
- [ ] 6. Créer repositories: ConversationRepository.java, MessageRepository.java
- [x] 7. Créer ChatService interface + impl (+ ChatMapper)
- [x] 8. Créer ChatController.java (/api/chat/...)

### Phase 3: Module Réseau Social (À créer)
- [ ] 9. Créer entities: Post.java, Interaction.java, Abonnement.java
- [ ] 10. Créer DTOs pour Post/Interaction/Abonnement
- [ ] 11. Créer repositories
- [ ] 12. Créer SocialService + impl
- [ ] 13. Créer SocialController.java (/api/social/...)

### Phase 4: Tests & Validation
- [ ] 14. Lancer serveur: cd backendComping && mvn spring-boot:run
- [ ] 15. Tester endpoints via Swagger /swagger-ui.html
- [ ] 16. Vérifier interconnexions (avisId in Post/Conversation)

Progression: Mise à jour après chaque étape.

