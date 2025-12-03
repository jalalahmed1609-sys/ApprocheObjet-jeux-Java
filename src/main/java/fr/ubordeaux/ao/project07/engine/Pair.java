package fr.ubordeaux.ao.project07.engine;

import java.util.Objects;

public class Pair<F, S> { 
    
    public final F first;
    public final S second;
    
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Pair)) return false;
        Pair<?, ?> other = (Pair<?, ?>) obj;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
