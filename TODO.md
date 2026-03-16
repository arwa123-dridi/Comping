# TODO.md - Plan pour corriger le code et rendre l'app exécutable

## Étapes du plan (cocher au fur et à mesure):

- [ ] 1. Créer/mettre à jour ce TODO.md ✅ **(EN COURS)**
- [x] 2. Fix SignupRepository.java - Ajouter méthode Spring Data findByEmail(String email) ✅
- [x] 3. Fix SignupServiceImpl.java - Injecter deps (@RequiredArgsConstructor SignupRepository, PasswordEncoder, JwtUtils), corriger registerUser/login logic, supprimer static refs/logs console ✅
- [x] 4. Fix GlobalExceptionHandler.java - Corriger signatures méthodes @ExceptionHandler (retour type sur 1 ligne), supprimer duplicatas ✅
- [x] 5. Fix SignupController.java - Remplacer @Autowired par constructor injection ✅
- [x] 6. Fix AvisServiceImpl.java - Ajouter imports ResponseStatusException/HttpStatus, ajouter méthode private mapToResponseDTO(Avis), corriger accents/apostrophes français ✅
- [x] 7. Vérifier repositories (AvisRepository, ReponseAvisRepository) pour méthodes custom ✅ (compile success)
- [x] 8. mvn clean compile - Vérifier compilation ✅ BUILD SUCCESS
- [ ] 9. mvn spring-boot:run - Lancer app, tester Swagger http://localhost:8087/swagger-ui.html
- [ ] 10. Tests fonctionnels (signup, avis, etc.) avec MongoDB local

**Prochaine étape: 2. SignupRepository.java**

