# PAC_FINALE — installation et activation

## État de la livraison

Le profil, l'inventaire, les achats, les résultats, les classements et les amis utilisent Firebase. Les décisions de partie et la monnaie passent par des fonctions authentifiées. Le téléphone n'a pas le droit de modifier ses points ou son inventaire directement.

**Le bouton « Jouer sans compte » démarre immédiatement une partie de découverte solo, sans serveur ni connexion.** Les fantômes et déplacements sont simulés ; toucher la carte permet de marcher et toucher un fantôme permet de le poursuivre. Les résultats de cette session restent locaux à la partie et ne sont pas envoyés au classement.

Le code doit être déployé avant d'utiliser les fonctionnalités en ligne contre Firebase réel. Aucun déploiement en production n'a été effectué depuis le poste de développement faute de session Firebase connectée. Google Play Jeux n'est pas encore créé dans la Play Console. Le bouton « Jouer en ligne » conserve l'accès aux salons ; les erreurs de serveur concernent uniquement ces fonctionnalités. Le fond cartographique peut nécessiter Internet pour télécharger les tuiles même en mode solo.

## 1. Ouvrir dans Android Studio

Ouvrir le dossier racine `PacMan-seek`, contenant `settings.gradle.kts`, et non le sous-dossier `app`.

- Installer le SDK Android 35 (le SDK minimal est 26).
- Utiliser Java 21 pour le daemon Gradle (le projet contient ses critères JVM). Le code Android cible Java 17.
- Vérifier `local.properties` : `sdk.dir` doit pointer vers le SDK local. Ce fichier n'est pas versionné.
- Synchroniser Gradle puis exécuter `app` sur un appareil.

L'installation Android Studio du poste de Theo a été réinstallée avec l'installateur officiel pendant cette livraison. Java embarqué et `product-info.json`, auparavant absents, sont maintenant présents.

## 2. Activer Firebase

Projet prévu : `pac-and-seek`. Région des fonctions : `europe-west1`.

1. Vérifier que `app/google-services.json` correspond à ce projet et au package `com.cachecache.game`.
2. Dans Firebase Authentication, activer la connexion anonyme pour les invités. Activer ensuite le fournisseur Play Games.
3. Créer/activer Firestore et vérifier les prérequis de facturation pour Cloud Functions et Cloud Scheduler dans la console avant de déployer.
4. Depuis le dossier `functions`, avec Node 22 :

```sh
npm ci
npx firebase login
npx firebase deploy --only functions,firestore --project pac-and-seek
```

Les règles **et** les fonctions doivent être livrées ensemble. Les anciens clients, qui écrivaient eux-mêmes l'état des parties, ne sont pas compatibles avec ces règles. Terminer les anciennes parties et mettre les appareils de test à jour avant bascule. Ne pas remplacer les règles par `allow write: if true` pour contourner une erreur.

Les index sont dans `firestore.indexes.json`. Attendre leur construction avant de tester la clôture planifiée des parties expirées. Le déclencheur `finishExpiredGames` clôt les parties expirées chaque minute ; les clients peuvent également demander la clôture, sans choisir le vainqueur.

## 3. Google Play Jeux

La Play Console ne contient pas encore ce jeu : cette étape reste à réaliser avec le propriétaire du compte.

1. Créer l'application et configurer Play Games Services v2 pour le même projet Google/Firebase.
2. Créer les identifiants OAuth Android (package et empreintes SHA-1 de signature) et serveur Web ; activer le fournisseur Play Games dans Firebase Authentication selon la documentation Google.
3. Ajouter les comptes testeurs. Pour une version Play, ajouter aussi l'empreinte du certificat Play App Signing.
4. Définir les deux identifiants publics dans les propriétés Gradle locales ou la configuration de compilation :

```properties
playGamesProjectId=IDENTIFIANT_NUMERIQUE_PLAY_GAMES
playGamesWebClientId=IDENTIFIANT_CLIENT_WEB.apps.googleusercontent.com
```

Ne jamais placer de secret OAuth dans l'application, le dépôt ou ces propriétés. Les identifiants ci-dessus sont publics. Recompiler après leur configuration. Tant qu'ils manquent, le bouton de connexion explique que cette version n'est pas configurée.

Le premier rattachement d'un invité à un nouveau compte Play Jeux conserve le même UID Firebase et sa progression. Si le compte Play Jeux possède déjà un profil, une confirmation explicite permet d'ouvrir celui-ci ; les deux progressions ne sont pas fusionnées. Le changement de compte est bloqué tant qu'un salon ou une partie est actif.

Documentation :
- https://firebase.google.com/docs/auth/android/play-games
- https://developer.android.com/games/pgs/android/android-signin
- https://firebase.google.com/docs/functions/callable

## 4. Tester sans serveur de production

```sh
cd functions
npm ci
npx firebase emulators:start --only auth,firestore,functions --project pac-and-seek
```

Depuis un second terminal à la racine :

```sh
./gradlew :app:connectedDebugAndroidTest -PuseFirebaseEmulators=true
```

L'émulateur Android accède au poste hôte via `10.0.2.2`. Ports : Auth 9099, Firestore **8188**, Functions 5001, interface de test 4000. Le port 8080 est déjà utilisé sur le poste. Pour un vrai téléphone, adapter l'adresse réseau de développement ; ne pas distribuer une compilation de test utilisant les émulateurs.

```sh
# Tests serveur et règles sur un projet démo isolé
cd functions
npm run test:emulator

# Compilation normale et vérifications Android
cd ..
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

## Modèle de données et comportement

- `users/{uid}` : données privées, monnaie, XP, statistiques, skins possédés et partie active.
- `players/{uid}` : profil public du classement. Aucun solde de monnaie ou position GPS.
- `users/{uid}/history/{gameCode}` : résultat unique par partie, écrit dans la transaction de récompense.
- `seasons/{YYYY-MM}/scores/{uid}` : score du mois UTC, distinct du total historique.
- `games/{code}` et `players` : salon puis partie ; seules les personnes membres lisent les positions. Les rôles, captures et fins de partie sont contrôlés par le serveur.
- `users/{uid}/friends`, `requests`, `invites` : liens acceptés, demandes reçues, invitations.
- `functions/catalog.json` : prix et déblocages validés par le serveur, cohérents avec les illustrations embarquées.

Une partie accepte 2 à 12 joueurs. L'hôte est Pacman et les autres joueurs doivent se déclarer prêts. Il choisit le temps de cache (30 s à 10 min côté serveur) et de chasse (1 à 60 min). Le départ de l'hôte annule la partie sans récompenses ; quitter comme dernier fantôme annule aussi la partie. Une partie interrompue peut être reprise depuis le profil ou le code du salon.

Les points de classement ne se dépensent pas. Une partie validée rapporte 20 points, 50 XP et 25 pièces de base ; chaque capture de Pacman ajoute 50 points, 25 XP et 10 pièces ; une victoire ajoute 100 points, 100 XP et 50 pièces. Un fantôme capturé ne reçoit pas le bonus de survie. Les paliers de niveau débloquent leurs skins sans achat.

Le GPS conserve un cap stable à l'arrêt et le rendu choisit parmi six rotations de 60° pour chaque illustration équipée. Les illustrations existantes sont réutilisées par rotation ; ce ne sont pas six dessins de perspective distincts. Pacman classique et Néon disposent d'un rendu dessiné dans le code. Les zones de cache restent anonymes sur la carte ; le skin s'affiche sur les marqueurs effectivement visibles.

Les Pac-dots sont un **guidage piéton**, pas une monnaie à ramasser. Le chemin se raccourcit au déplacement et se recalcule périodiquement. Si le service d'itinéraire échoue, le jeu l'indique et ne trace pas de faux raccourci à travers les bâtiments. Le routage dépend du service OSM public ; prévoir un service dédié si l'usage augmente.

La capture doit être confirmée en ligne, pendant la phase de recherche, avec deux positions de moins de 30 s et une précision annoncée de 25 m ou mieux, à moins de 15 m. Les écritures de positions trop anciennes sont refusées. Ces seuils devront être calibrés dehors : ni le GPS ni le RSSI Bluetooth ne garantissent une mesure de 30 cm. Le Bluetooth sert d'aide à la recherche et ne remplace pas la validation serveur. Les coordonnées remontées par un téléphone peuvent être falsifiées ; ce contrôle n'est pas un système anti-triche complet.

## Validation terrain à faire avant diffusion

- Deux vrais téléphones, puis plusieurs : salon, prêts, lancement commun, recherche, capture, résultat et historique identiques.
- GPS imprécis/refusé, Bluetooth indisponible, pertes de réseau, fermeture puis reprise, départ de l'hôte.
- Achat, équipement et visibilité des skins ; conservation après reconnexion Play Jeux.
- Vérifier les règles et index réellement déployés, surveiller les erreurs et les coûts Firebase.
- Les parties sont conçues pour rester au premier plan avec l'écran allumé ; le suivi continu écran verrouillé n'est pas implémenté.
- Supprimer/archiver périodiquement les anciennes positions et parties ; définir la politique de conservation et les réglages de protection contre les abus avant ouverture publique.

Les tests locaux ne remplacent pas ces essais sur appareils ni la validation des identifiants Play Jeux.
