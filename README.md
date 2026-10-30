# Pac & Seek — PAC_FINALE

Application Android Kotlin / Jetpack Compose de cache-cache géolocalisé. Profils, boutique, pass, classements mondial/mensuel/amis, salons par code et invitations utilisent Firebase Authentication et Cloud Firestore sur Spark, sans Cloud Functions. Connexion Google classique facultative ; pas besoin de Play Jeux.

**Jouer sans compte** démarre une découverte solo simulée sans Firebase. Le jeu en ligne crée une identité anonyme, qui peut ensuite être liée à Google.

Voir [DEPLOYMENT.md](DEPLOYMENT.md) pour configurer les fournisseurs, publier les règles dans la base existante et installer l'APK. Les anciennes parties sont conservées ; créer une nouvelle partie pour tester.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
python tools/generate_firestore_rules.py --check
cd functions
npm ci
npm run test:emulator
```

Transactions : `SparkService.kt`. Autorisations : `firestore.rules`. Après un changement de prix ou de palier dans `functions/catalog.json`, régénérer avec `python tools/generate_firestore_rules.py`, garder le catalogue Kotlin cohérent et relancer les tests.

Salons de 2–12 joueurs, état prêt, chronos partagés et reprise par code. GPS demandé chaque seconde ; envoi environ toutes les deux secondes en mouvement, cinq secondes à l'arrêt. Le multijoueur consomme les quotas Spark : voir le guide pour les limites.

Illustrations d'origine conservées : Pacman regarde à gauche ou à droite, fantômes classiques avec six vues debout. Le pass conserve ses dix paliers et ses récompenses exclusives.

Voir [VALIDATION.md](VALIDATION.md) pour les vérifications réellement effectuées et les limites.
