# CAI-Android

L'application permet à deux utilisateurs (sur deux appareils différents) de s'échanger une balle qui rebondit d'un écran à l'autre.
La communication entre les deux utilise le bluetooth.
Pour établir la connexion, il est nécessaire que les deux utilisateurs choisissent l'option multi-joueur (la première) sur l'option principal.
Ensuite un des utilisateurs sélectionne l'autre dans la liste (il peut être nécessaire de rendre l'appareil visible d'abord en cliquant sur le bouton en haut).
Lorsque la connexion est établie, l'activité de jeu se lance sur les deux écrans. 
La balle - sur l'écran de l'hôte (celui qui n'a pas lancé la connexion) - peut ensuite être lancée vers l'autre écran.
Un compteur de score est incrémenté sur les deux appareils quand la balle touche une des zones rouges.

Il est possible de faire rebondir la balle avec un seul appareil en choisissant la seconde option, 
dans ce cas aucune communication n'a lieu.
