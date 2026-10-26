# Pac & Seek — PAC_FINALE

Application Android Kotlin / Jetpack Compose de cache-cache géolocalisé, avec comptes Google Play Jeux/Firebase, salons en ligne, profil persistant, boutique, classements et amis.

**Commencer par [DEPLOYMENT.md](DEPLOYMENT.md)** pour installer le projet, déployer Firebase et configurer Play Jeux. Le serveur et les identifiants Play Jeux doivent être activés avant utilisation en production. La branche contient aussi une suite de tests isolés sur émulateurs Firebase.

## Vérification

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
cd functions
npm ci
npm run test:emulator
```

- Profils, XP, monnaie et inventaires enregistrés dans Firebase.
- Achats et récompenses transactionnels, points de classement distincts des pièces.
- Salons 2–12 joueurs, état prêt, reprise par code et chronos partagés.
- GPS réel, radar Bluetooth, captures confirmées par le serveur et guidage Pac-dots piéton.
- Skins équipés transmis aux joueurs, six orientations par illustration.
- Classements général, mensuel UTC et amis, pagination et rang personnel.
- Demandes d'amis et invitations aux salons.

Les essais dehors sur plusieurs téléphones et l'activation Play Console restent nécessaires. Voir les limites et la procédure détaillée dans le guide de déploiement.
