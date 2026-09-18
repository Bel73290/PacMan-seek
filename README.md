# PacMan-seek

Application mobile native Android **"Cache Cache"** développée sous **Jetpack Compose** et **Kotlin**, inspirée de l'univers Pac-Man et conçue sur mesure à partir de la maquette de jeu géolocalisé.

## Fonctionnalités Clés & 0 Bouton Mort

1. **100 % Interactif & Autonome :**
   - Boucle de jeu complète fonctionnelle sans serveur externe grâce à un moteur de simulation dynamique intégré.
   - Timers réactifs configurables (Chercheur 3 min, Cachés 10 min par défaut).
   - Détection de proximité géodésique (Haversine) en temps réel.
   - Mode panique du fantôme : bascule en bleu électrique (`#2E86DE`) avec bouche ondulée et animation de tremblement panique dès que la distance passe sous 25 mètres.
   - **Règle stricte des 30 cm** : la capture d'un caché (+50 points) est validée **uniquement** si le chercheur arrive à moins de 30 cm ($\le 0{,}30$ m) de la cible.
   - Simulation de marche intégrée permettant de faire progresser le chercheur en continu vers une cible ou le long des Pac-dots jusqu'au contact immédiat (30 cm).

2. **Cartographie OpenStreetMap (OSMDroid) :**
   - Données géolocalisées réelles sur le campus de l'**Université Savoie Mont-Blanc (USMB) - Savoie Technolac** (Le Bourget-du-Lac).
   - Points d'intérêt réels : Polytech Annecy-Chambéry, BU Bourget, Bâtiment Éveillon, RU La Boussole, Hall des Sports USMB.
   - Calque cartographique sombre adapté au thème.
   - Zone suspecte translucide avec polygone pointillé englobant les cachés.
   - Cheminement de pastilles jaunes animées ("Pac-dots") menant le chercheur à la zone.
   - Onde radar pulsée concentrique émanant du joueur à 60 fps.

3. **ZÉRO EMOJI - Graphismes 100 % Vectoriels & Canvas :**
   - Tous les personnages (Pacman avec bouche animée, fantômes aux couleurs officielles, fantôme apeuré avec bouche zigzag et tremblement), trophées (Or, Argent, Bronze), étoiles et badges géométriques sont dessinés sur mesure via Jetpack Compose Canvas et VectorDrawables XML.

4. **Les 8 Écrans Complets de la Maquette :**
   - **Écran 1 : Splash / Accueil** : Logo 3D stylisé, perspective map, Pacman mangeant des pastilles, fantôme apeuré, bouton "Jouer", bouton "Classement", icône d'engrenage active ouvrant la boîte de dialogue de réglage des timers avec steppers `-` / `+`.
   - **Écran 2 : Partie en cours** : En-tête avec double compteur (Chercheur et Cachés) décomptant chaque seconde, carte interactive OSM, marqueur rotatif selon le cap de marche, bandeau d'instructions et bouton recentrer GPS.
   - **Écran 3 : Zone suspecte & Guidage** : Zoom sur la zone suspecte délimitée par polygone bleu, Pac-dots animés, modale haute avec bouton d'action "Aller dans cette zone".
   - **Écran 4 : Pop-up de Capture ("Tu as trouvé un caché !")** : Apparition rebondissante, étincelles rayonnantes, grand fantôme apeuré animé, compteur fluide "+ 50 points", temps restant, identifiant du caché, position sur le campus, bouton "Continuer".
   - **Écran 5 : Écran Classement (Leaderboard)** : Onglets "Général", "Amis", "Saisons", podium avec trophées or/argent/bronze, ligne "Toi" mise en avant avec score synchronisé, navigation inférieure.
   - **Écran 6 : Vue Radar & Filtres** : Vagues radar concentriques continues, bouton "Filtres" ouvrant le menu de filtres (Tous, Proches <50m, Zone suspecte, Apeurés) filtrant dynamiquement les marqueurs sur la carte.
   - **Écran 7 : Guide du Jeu ("Le principe")** : 4 étapes illustrées détaillant les règles avec mise à jour dynamique des timers configurés et rappel du seuil de 30 cm, bouton "C'est parti !".
   - **Écran 8 : Profil Utilisateur & Statistiques** : Carte joueur avec avatar Pacman, "Toi Niveau 5", barre XP dynamique, métriques clés (Points, Parties, Victoires), grille de badges vectoriels cliquables ("Premier trouvé", "Série de victoires", "Explorateur urbain") avec dialogue de détail, historique récent.
   - **Barre de navigation inférieure** : Navigation persistante entre Carte, Classement et Profil avec mise en surbrillance jaune `#FFC814`.

## Stack Technique

- **Langage :** Kotlin 2.0.21, Coroutines, StateFlow
- **UI Framework :** Jetpack Compose (BOM 2024.10.01, Material 3)
- **Cartographie :** OpenStreetMap via OSMDroid 6.1.20
- **Architecture :** Clean Architecture + MVI/MVVM, State Hoisting
- **SDK :** Android 15 (compileSdk 35, minSdk 26)
- **Outil de Build :** Gradle 8.13, AGP 8.13.0
