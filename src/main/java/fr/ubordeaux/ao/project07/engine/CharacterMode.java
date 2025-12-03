package fr.ubordeaux.ao.project07.engine;

public interface CharacterMode extends ICharacterMode {

    int getNumFrames();

    boolean isLoop();

    int ordinal();

    String name();

    float getFps();

}
