# PAC_FINALE : activer Firebase gratuit

## La base existe déjà

La capture montre Cloud Firestore `(default)` et cinq anciennes parties dans `games`. Ne crée pas une Realtime Database et ne supprime pas cette base. Le projet est `pac-and-seek`.

La nouvelle version utilise Authentication et Firestore directement : aucune Cloud Function, aucun ordonnanceur, aucun abonnement Blaze. Les transactions Android et les règles contrôlent les achats, les paliers du pass et les récompenses uniques. L'ancien backend est archivé dans `functions/archive` et n'est pas déployé.

Les anciennes parties restent intactes et lisibles par leurs participants, sans modifications ni nouvelles récompenses. Créer une nouvelle partie pour tester : elle aura `schemaVersion: 2`.

## 1. Publier les règles et index

Firebase Console → Cloud Firestore → Règles : remplacer le contenu par **firestore.rules**, puis **Publier**. Cela modifie les autorisations de toute la base : installer la nouvelle version sur tous les téléphones de test. Aucun document ancien n'est effacé.

Deux index composites de portée Collection sont fournis dans `firestore.indexes.json` : collections `players` et `scores`, champ `score` décroissant puis `__name__` croissant. Attendre qu'ils soient prêts.

Pour publier règles et index ensemble, depuis la racine du projet :

```powershell
cd functions
npm ci
npx firebase login
npx firebase deploy --only firestore --project pac-and-seek
```

Ne pas déployer de Cloud Functions. Aucun déploiement réel n'a été effectué depuis ce poste : aucune session Firebase CLI n'est connectée.

Les profils `users`, fiches publiques `players`, codes `friendCodes`, amis, invitations, historiques et `seasons/{AAAA-MM}/scores` seront créés automatiquement par l'app. Rien à saisir manuellement pour les pièces, profils ou skins.

## 2. Activer les invités

Authentication → Commencer si nécessaire → Méthode de connexion → **Anonyme** → Activer → Enregistrer.

En ligne, Firebase attribue un identifiant invité au téléphone. Une progression invitée peut être perdue si l'identité locale est supprimée avant d'être liée à Google. **Jouer sans compte** reste une découverte solo indépendante de Firebase, sans scores en ligne.

## 3. Activer Google classique

Cette version utilise Credential Manager et Firebase Authentication ; pas besoin de créer un jeu Google Play Jeux dans la Play Console.

1. Authentication → Méthode de connexion → **Google** → Activer, choisir l'adresse d'assistance et enregistrer.
2. Paramètres du projet → Général → Application Android `com.cachecache.game` → Ajouter l'empreinte SHA-1 de la signature utilisée.
3. SHA-1 de l'APK debug de ce poste : `64:B1:A6:54:A2:26:63:A8:D1:72:57:A7:A0:59:DE:14:8F:FE:45:EB`.
4. Télécharger à nouveau `google-services.json`, remplacer `app/google-services.json`, reconstruire l'APK. Le fichier doit fournir `default_web_client_id`. Une autre signature, notamment celle de publication, exige sa propre empreinte.
5. Sur un téléphone avec les services Google Play, ouvrir le profil puis **Se connecter avec Google**. Un nouveau compte Google est lié à l'invité en conservant la progression. Si ce compte a déjà un profil, l'app demande de choisir explicitement son ouverture ; aucune fusion automatique.

La connexion Google réelle reste à tester après cette configuration. Le fichier du dépôt ne suffit pas à activer le fournisseur dans la console.

## 4. Installer et tester

Ouvrir le dossier contenant `settings.gradle.kts` dans Android Studio. SDK Android 35, minimum Android 8 / API 26. Java 21 pour Gradle ; le code Android cible Java 17.

```powershell
.\gradlew.bat :app:assembleDebug -PuseFirebaseEmulators=false
```

Installer `app/build/outputs/apk/debug/app-debug.apk` sur deux téléphones. Accorder la localisation précise et les autorisations Bluetooth demandées. Tester dehors avec Internet :

1. Sauvegarder deux pseudos invités et vérifier leur persistance après redémarrage.
2. Échanger les codes amis, accepter une demande, vérifier le classement entre amis.
3. Créer une nouvelle partie, transmettre le code ou inviter l'ami, rejoindre, se déclarer prêt et lancer.
4. Vérifier les chronos, les positions, la capture à proximité, la fin et les récompenses uniques. Vérifier les classements mondial et mensuel, puis acheter/équiper un skin une fois assez de pièces gagnées. Le pass nécessite les niveaux requis.
5. Tester une perte de réseau, la reprise par code et l'annulation par l'hôte. Les récompenses non récupérées sont traitées au retour dans l'app.

## Positions fréquentes et quotas

GPS haute précision demandé chaque seconde. Position envoyée environ toutes les **2 secondes** si le joueur a bougé d'au moins un mètre, et toutes les **5 secondes** à l'arrêt. Firestore diffuse les changements par écoute temps réel. Ce sont des cadences visées : le réseau et Android peuvent retarder une mise à jour. Un GPS reste souvent imprécis de plusieurs mètres ; cette cadence ne garantit pas une position exacte.

L'hôte écoute les joueurs ; chaque fantôme écoute sa propre position et celle de l'hôte pour limiter les lectures. Les positions trop anciennes ne permettent pas une capture. Les règles contrôlent les phases et une proximité GPS approximative ; elles ne peuvent pas empêcher un téléphone de falsifier son GPS. Une protection anti-triche forte demanderait un serveur de confiance.

Spark est gratuit dans ses quotas, notamment 50 000 lectures et 20 000 écritures Firestore par jour et 1 Gio de stockage gratuit. Un téléphone qui envoie toutes les deux secondes représente environ 1 800 écritures par heure, plus les autres opérations. Surveiller Usage dans Firebase : de nombreuses longues parties peuvent épuiser le quota et interrompre le service jusqu'à son renouvellement. Rester sur Spark évite une facturation à l'usage automatique.

Il n'y a pas de tâche serveur en arrière-plan : une partie expirée est clôturée quand un participant connecté la traite. Chacun récupère ses propres récompenses en ligne, éventuellement à sa prochaine ouverture.

Références : [quotas Firestore](https://firebase.google.com/docs/firestore/quotas), [connexion Google Android](https://firebase.google.com/docs/auth/android/google-signin).
