package fr.ubordeaux.ao.project07.engine;

/**
 * Enumération représentant les directions possibles pour un personnage.
 * Chaque direction est associée à un index et à un angle en degrés.
 */
public enum Direction {
    /** Sud-Est, angle 135° */
    SOUTHEAST(0, 135),

    /** Est, angle 90° */
    EAST(1, 90),

    /** Nord-Est, angle 45° */
    NORTHEAST(2, 45),

    /** Nord, angle 0° */
    NORTH(3, 0),

    /** Nord-Ouest, angle 315° (ou -45°) */
    NORTHWEST(4, 315),

    /** Ouest, angle 270° */
    WEST(5, 270),

    /** Sud-Ouest, angle 225° (ou -135°) */
    SOUTHWEST(6, 225),

    /** Sud, angle 180° */
    SOUTH(7, 180);

    /** Index ordinal de la direction */
    private final int value;

    /** Angle de la direction en degrés */
    private final int angle;

    private Direction(int value, int angle) {
        this.value = value;
        this.angle = angle;
    }

    public int getValue() {
        return value;
    }

    public double getRadAngle() {
        return angle * Math.PI / 180;
    }

    public int getAngle() {
        return angle;
    }
}
