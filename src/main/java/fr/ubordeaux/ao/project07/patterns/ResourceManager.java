package fr.ubordeaux.ao.project07.patterns;

/**
 * Singleton pour la gestion des ressources du jeu (sons, images, etc.)
 */
public class ResourceManager {
    private static ResourceManager instance;

    private ResourceManager() {
        // Initialisation des ressources
    }

    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    // Exemple de méthode pour charger une ressource
    public void loadResource(String name) {
        // ...
    }
}
