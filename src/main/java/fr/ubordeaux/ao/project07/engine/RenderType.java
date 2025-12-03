package fr.ubordeaux.ao.project07.engine;

/**
 * Enumération représentant le type de rendu pour un objet dans le jeu.
 * 
 * Les types disponibles sont :
 * 
 * - {@link #FLOOR} : utilisé pour les éléments du sol.
 * - {@link #CONSTRUCTION} : utilisé pour les objets ou personnages à dessiner
 * par-dessus le sol.
 * 
 * 
 */
public enum RenderType {
    FLOOR, CONSTRUCTION, ITEM
}
