package fr.ubordeaux.ao.project07.engine;

import java.awt.Color;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Interface générique représentant un personnage ou une entité animée dans le jeu.
 *
 * 
 * Cette interface définit le comportement commun à tout personnage affichable
 * dans le moteur isométrique (par exemple : {@link Crusader}, {@link Spider}, etc.).
 * 
 *
 * 
 * Elle permet de :
 * 
 *   -positionner le personnage dans l’espace 3D (x, y, z)
 *   -changer son orientation (direction)
 *   -modifier son état ou animation (mode)
 *   -définir des callbacks déclenchés à différents moments des animations
 *   -ajuster la taille (scale) ou le décalage (offset) visuel
 * 
 * 
 *
 * @param <S> Type représentant les modes possibles du personnage
 *            (par ex. {@link Crusader.Mode} ou {@link Spider.Mode})
 */
public interface ICharacter<S extends ICharacterMode> {

    // =========================
    // 📍 POSITION ET DIRECTION
    // =========================

    /**
     * Définit la position du personnage dans l’espace du jeu.
     *
     * @param x position horizontale (axe des colonnes)
     * @param y position verticale (axe des lignes)
     * @param z hauteur (niveau, pour effets de saut ou décalage vertical)
     */
    void setPosition(float x, float y, float z);

    /**
     * Définit la position de l'ombre du personnage dans l’espace du jeu.
     *
     * @param x position horizontale (axe des colonnes)
     * @param y position verticale (axe des lignes)
     * @param z hauteur (niveau, pour murs)
     */
    void setShadowPosition(float x, float y, float z);

    /**
     * Définit la direction vers laquelle le personnage est orienté.
     *
     * @param targetDirection direction cible (ex. {@link Direction#NORTH})
     * @param time durée de la rotation vers la direction en ms
     */
    void setDirection(Direction targetDirection, int time);

    /**
     * Définit la direction vers laquelle le personnage est orienté.
     *
     * @param direction direction cible (ex. {@link Direction#NORTH})
     */
    void setDirection(Direction direction);

    /**
     * Définit la direction vers laquelle le personnage est orienté.
     *
     * @param directionName nom de la direction cible
     */
    void setDirection(String directionName);

    // =========================
    // 🎬 MODES / ANIMATIONS
    // =========================

    /**
     * Définit le mode actuel du personnage.
     * 
     * Le mode détermine l’animation à jouer (ex. {@code WALK}, {@code RUN}, {@code ATTACK}, etc.).
     * 
     *
     * @param mode mode du personnage
     */
    void setMode(S mode);

    /**
     * Définit le mode actuel du personnage.
     * 
     * Le mode détermine l’animation à jouer
     * 
     *
     * @param modeName mode du personnage
     */
    void setMode(String modeName);

    /**
     * Définit la vitesse d'animation
     * 
     *
     * @param mode
     * @param fps vitesse
     */
    void setFrameRate(S mode, float fps);

    // =========================
    // ⏱️ CALLBACKS D’ANIMATION
    // =========================

    /**
     * Déclencheur appelé au début de l’animation.
     * 
     * Peut servir à initialiser un effet ou un son.
     * 
     *
     * @param callback fonction appelée au début de l’animation
     */
    void setBeginAnimationTrigger(Predicate<ICharacter<?>> callback);

    /**
     * Déclencheur appelé au milieu de l’animation.
     * 
     * Souvent utilisé pour synchroniser un effet (par exemple : impact d’une attaque).
     * 
     *
     * @param callback fonction appelée à la moitié de l’animation
     */
    void setMidAnimationTrigger(Predicate<ICharacter<?>> callback);

    /**
     * Déclencheur appelé à la fin de l’animation.
     * 
     * Peut servir à remettre le personnage à l’état {@code IDLE}, ou enchaîner une autre animation.
     * 
     *
     * @param callback fonction appelée à la fin de l’animation
     */
    void setEndAnimationTrigger(Predicate<ICharacter<?>> callback);

    /**
     * Déclencheur appelé à chaque "tick" d’animation (frame ou mise à jour du jeu).
     * 
     * Idéal pour mettre à jour dynamiquement la position, la physique ou d’autres paramètres.
     * 
     *
     * @param callback fonction appelée à chaque tick d’animation
     */
    void setTickAnimationTrigger(Predicate<ICharacter<?>> callback);

    // =========================
    // 🧩 APPARENCE VISUELLE
    // =========================

    /**
     * Définit un facteur d’échelle pour le personnage.
     * 
     * Exemple : 1.0 = taille normale, 2.0 = deux fois plus grand.
     * 
     *
     * @param scale facteur d’échelle (> 0)
     */
    void setScale(float scale);

    /**
     * Définit la position du personnage.
     * 
     * @param x
     * @param y
     */
    void setOffset(float x, float y);

    /**
     * Donne l'identificateur du personnage.
     * 
     */
    UUID getId();

    float getFrameIndex();
    
    /**
     * Définit la luminosité du personnage.
     * 
     * @param brightness
     */
    void setBrightness(float brightness);

    void setColor(Color color);

    void resetAnimation();

}
