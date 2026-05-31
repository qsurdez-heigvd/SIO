SIO - Travail pratique 2
------------------------

Mise en place
-----------------

- Créer un nouveau projet "maven" à partir des sources fournies.
- Renommer le répertoire groupX (Shift + F6) selon le nom de votre groupe.
- En cas de problème avec le look and feel FlatLaf il suffit de commenter l'avant dernière ligne de GUI.java pour supprimer la dépendance.

Brève description des répertoires
---------------------------

- display : contient le code lié à l'interface utilisateur et les interfaces ObservableTspConstructiveHeuristic et TspHeuristicObserver.

- tsp : contient le code permettant la gesion d^un jeu de données (TspData), le stockage d'une solution (TspTour) ainsi que l'interface TspConstructiveHeuristic.

- sample : contient le code de deux heuristiques triviales pour le TSP :
    - CanonicalTour : construit une tournée en ajoutant successivement les villes dans l'ordre croissant de leur numéro 0->1->2...
    - RandomTour : construit une tournée en ajoutant successivement les villes dans un ordre aléatoire. Vous pouvez utiliser cette heuristique comme point de départ pour vos algorithmes.

- groupeX (à renommer selon le nom de votre groupe) : contient deux classes exécutables :
    - GUI : programme ouvrant une interface graphique permettant la visualisation et l'animation de vos heuristiques,
    - Analyze : programme à utiliser pour le calcul des performances des deux heuristiques.
    - **Vous placerez toutes vos implémentations dans ce répertoire (et d'éventuels sous-répertoires) et uniquement dans ce répertoire.**
    - Pour le rendu de votre travail vous compresserez ce répertoire au format zip et le déposerez sur Cyberlearn.

Comment commencer
-----------------

- Pour implémenter votre première heuristique du plus proche voisin (*Nearest Neighbor*) vous pouvez partir d'une copie de RamdonTour et ajouter votre algorithme dans GUI.java.

- RandomTour est composé de deux parties :
  1.  Un Traversal qui permet l'animation de l'agorithme en itérant sur les arêtes à afficher à chaque mise à jour de l'interface graphique. Quelques ajustements mineurs seront peut-être nécessaires selon vos choix de structures de données.
  2. La méthode computeTour qui calcule la tournée et retourne la solution sous forme d'un record TspTour.
- L'objet de type TspData passé à computeTour vous donne accès, entre autres,
     -  au nombre de villes de jeu de données (TspData.getNumberOfCities()) qui sont numérotées à partir de 0 ;
     -  à la distance entre deux villes i et j (TspData.getDistance(i,j)).

- À la fin de RandomTour vous trouverez deux tests de cohérence de la solution produite (validation du calcul de la longueur de la tournée et vérification que tous les sommets sont présents une et une seule fois dans la solution) qui pourront vous être utiles pendant la phase de mise au point de votre algorithme.

- En plus des jeux de données à analyser, le répertoire data contient également les données pour les deux exercices de la série 2 et le fichier exemples-solutions.txt contient les détails de l'application des deux heuristiques pour ces données.
 