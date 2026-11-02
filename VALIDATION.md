# Vérification Spark — 30 octobre 2026

- Compilation APK debug et APK de tests Android réussie ; version normale, `USE_FIREBASE_EMULATORS=false`.
- Tests JVM : 3 réussis (orientations et sens gauche/droite).
- Android Lint : aucune erreur bloquante ; avertissements existants à consulter dans le rapport Gradle.
- Firebase local : 24 tests réussis, dont 18 tests des nouvelles règles Spark et 6 tests du catalogue/calculs. Ils vérifient les profils et accès privés, achats concurrents et prix exacts, paliers du pass, création/jonction/prêt/lancement, anciennes parties en lecture seule, mises à jour GPS, captures trop tôt/loin/périmées, fin de partie, récompenses uniques, amis et invitations.
- Cohérence du fichier de règles généré : vérifiée.

Les parcours Android ont été adaptés et compilés, mais n'ont pas été exécutés après cette migration : le disque C: ne dispose plus d'assez d'espace pour démarrer l'AVD Pixel 9 Pro (environ 1,2 Go libres au contrôle). Les parcours réussis le 26 octobre concernaient le backend précédent et ne valident pas la migration Spark.

Les règles/index et les fournisseurs Google/Anonyme ont été configurés manuellement par Theo selon ses confirmations ; aucun déploiement depuis le poste ni contrôle direct de la console n'a été effectué. Le nouveau google-services.json téléchargé le 30 octobre a été intégré : projet, package, empreinte debug SHA-1 et client OAuth Web vérifiés. APK reconstruit et 3 tests JVM réussis. La connexion Google réelle reste à tester sur téléphone. Les positions toutes les deux secondes sont une cadence visée, pas une mesure de latence sur réseau mobile. GPS, Bluetooth, itinéraires, reconnexion et consommation des quotas doivent être vérifiés sur deux vrais téléphones dehors.

Voir DEPLOYMENT.md pour les actions nécessaires dans la console Firebase existante.

## Carte et profil — 2 novembre 2026

- OpenStreetMap : correction du détachement prématuré de la vue lors de la première recomposition, cache dans le stockage privé de l'app et fond standard lisible. Contrôle Android du chargement de tuiles réelles dans le cache.
- Multijoueur : marqueurs des fantômes transmis à la carte côté Pacman ; position du Pacman affichée côté fantôme. Caméra distincte du marqueur et cadrage automatique des positions disponibles. Aucun bouton « Voir les joueurs » ajouté.
- Pac-dots : espacement de 28 dp, rayon de 2,5 dp, couleur chaude atténuée, sans halo ni défilement.
- Profil : bouton « Modifier mes infos », sélection en brouillon et barre fixe « Enregistrer les modifications » / « Annuler ». Confirmation de sauvegarde après les opérations Firebase. Le test de formulaire utilise un profil de démonstration et vérifie le prénom/pseudo et les skins envoyés à la sauvegarde ; il ne crée pas de données réelles.
- Vérifications : compilation debug, tests JVM et Android Lint ; quatre tests Android ciblés sur les marqueurs/carte, la modification/enregistrement/annulation du profil (clavier ouvert), la boutique sans skins Pacman et les deux sens du Pacman classique.

Les positions GPS physiques et le multijoueur sur deux téléphones réels restent à vérifier dehors. Les règles Firestore ne changent pas pour cette mise à jour.

Pacman conserve uniquement son apparence classique : les variantes ont été retirées du catalogue Android et des choix de boutique/profil. Les anciens identifiants enregistrés restent compatibles mais n'affichent plus de variante. Son composant accepte un sens gauche/droite, sans angle de rotation ; le marqueur utilise la même animation classique. Les fantômes conservent leurs skins et leurs six vues debout.
