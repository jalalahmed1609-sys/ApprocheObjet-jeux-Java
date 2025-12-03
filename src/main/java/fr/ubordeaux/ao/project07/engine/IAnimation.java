package fr.ubordeaux.ao.project07.engine;

import java.awt.image.BufferedImage;

interface IAnimation<S extends CharacterMode> {

    BufferedImage getCurrentFrame();

	public BufferedImage getCurrentShadow();

	public void reset();

	public void lastFrame();

    void tick(AbstractCharacter<? extends CharacterMode> owner);

}
