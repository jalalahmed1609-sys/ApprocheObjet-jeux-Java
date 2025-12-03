package fr.ubordeaux.ao.project07.patterns;

import fr.ubordeaux.ao.project07.Spider;
import fr.ubordeaux.ao.project07.Crusader;
import fr.ubordeaux.ao.project07.engine.ICharacter;

/**
 * Factory pour créer des ennemis du jeu
 */
public class EnemyFactory {
    public static ICharacter createEnemy(String type) {
        switch(type) {
            case "spider":
                return new Spider();
            case "crusader":
                return new Crusader();
            default:
                throw new IllegalArgumentException("Type d'ennemi inconnu : " + type);
        }
    }
}
