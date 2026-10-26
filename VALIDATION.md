# Vérification locale — 26 octobre 2026

Correctif solo : test Android supplémentaire réussi, sans émulateurs Firebase. Le bouton « Jouer sans compte » ouvre immédiatement une partie, ne crée pas de compte Firebase et permet de recommencer avec un score local remis à zéro. Compilation et tests JVM réussis après ce correctif.

- Compilation Android debug : réussie, version normale reliée à Firebase réel (`USE_FIREBASE_EMULATORS=false`).
- Contrôle Android Lint : aucune erreur bloquante ; des avertissements restent présents dans les ressources et le code existant.
- Tests JVM : 2 tests réussis (six orientations, normalisation et cap GPS).
- Firebase local : 19 tests réussis (règles d'accès, achats simultanés, récompenses uniques, captures, salons, skins, pagination, amis et invitations).
- Android 16 / Pixel 9 Pro émulé : 2 parcours réussis contre les services Firebase locaux :
  - Sauvegarde du pseudo, profil, inventaire, boutique, classement et accès aux amis.
  - Création de salon, second compte Firebase, validation de l'état prêt, lancement sur la carte, annulation par l'hôte.

Ces vérifications n'utilisent pas le serveur de production. Elles ne valident pas la connexion Play Jeux réelle, les permissions/configurations des consoles, la qualité GPS/Bluetooth dehors ni un déploiement Firebase. La carte a été ouverte dans l'émulateur ; le téléchargement complet des tuiles et le guidage GPS doivent encore être contrôlés sur de vrais téléphones.

Voir [DEPLOYMENT.md](DEPLOYMENT.md) pour activer les services et réaliser la validation terrain. Les six orientations des skins sont des rotations des illustrations existantes, pas six dessins de perspective distincts.
