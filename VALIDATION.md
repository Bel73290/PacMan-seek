# Vérification Spark — 30 octobre 2026

- Compilation APK debug et APK de tests Android réussie ; version normale, `USE_FIREBASE_EMULATORS=false`.
- Tests JVM : 3 réussis (orientations et sens gauche/droite).
- Android Lint : aucune erreur bloquante ; avertissements existants à consulter dans le rapport Gradle.
- Firebase local : 24 tests réussis, dont 18 tests des nouvelles règles Spark et 6 tests du catalogue/calculs. Ils vérifient les profils et accès privés, achats concurrents et prix exacts, paliers du pass, création/jonction/prêt/lancement, anciennes parties en lecture seule, mises à jour GPS, captures trop tôt/loin/périmées, fin de partie, récompenses uniques, amis et invitations.
- Cohérence du fichier de règles généré : vérifiée.

Les parcours Android ont été adaptés et compilés, mais n'ont pas été exécutés après cette migration : le disque C: ne dispose plus d'assez d'espace pour démarrer l'AVD Pixel 9 Pro (environ 1,2 Go libres au contrôle). Les parcours réussis le 26 octobre concernaient le backend précédent et ne valident pas la migration Spark.

Aucun déploiement réel de règles/index n'a été réalisé. Le compte Google réel reste à configurer et tester. Les positions toutes les deux secondes sont une cadence visée, pas une mesure de latence sur réseau mobile. GPS, Bluetooth, itinéraires, reconnexion et consommation des quotas doivent être vérifiés sur deux vrais téléphones dehors.

Voir DEPLOYMENT.md pour les actions nécessaires dans la console Firebase existante.
