package fr.ubordeaux.ao.project07.patterns;

public class MeleeAttack implements AttackStrategy {
    @Override
    public void attack() {
        System.out.println("Attaque au corps à corps !");
    }
}
