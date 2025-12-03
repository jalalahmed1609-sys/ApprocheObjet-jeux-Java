package fr.ubordeaux.ao.project07.engine;

/**
 * Représente un point discret dans l'espace 3D du plateau de jeu.
 * Utilisé pour indexer et stocker des objets ou personnages sur une grille.
 */
public class DiscretePoint {

    private final int x;
    private final int y;
    private final int z;

    public DiscretePoint(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.x;
        hash = 67 * hash + this.y;
        hash = 67 * hash + this.z;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DiscretePoint other = (DiscretePoint) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return this.z == other.z;
    }

}
