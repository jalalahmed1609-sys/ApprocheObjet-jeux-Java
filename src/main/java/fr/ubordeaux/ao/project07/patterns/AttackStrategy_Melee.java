package fr.ubordeaux.ao.project07.patterns;

public class AttackStrategy_Melee implements AttackStrategy {
    @Override
    public void attack() {
        System.out.println("Attaque au corps à corps !");
    }
}
