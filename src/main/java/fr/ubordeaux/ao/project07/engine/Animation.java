package fr.ubordeaux.ao.project07.engine;

import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Représente une animation pour un personnage.
 * 
 * 
 * Gère les frames d'images et d'ombres, la progression de l'animation,
 * les callbacks liés aux différentes étapes de l'animation, et le bouclage.
 * 
 *
 * @param <S> Type du mode de personnage (CharacterMode)
 */
public class Animation<S extends CharacterMode> implements IAnimation<S> {

	/** Tableau des images de l'animation */
	private final BufferedImage[] images; //

	/** Tableau des ombres associées aux images */
	private final BufferedImage[] shadows; // 

	/** Nombre total de frames */
	private final int frameNumber;

	/** Index approximatif de la frame médiane (milieu de l'animation) */
	private final int frameNumberMid;

	/** Indique si l'animation doit boucler */
	private final boolean loop;

	/** Indique si l'animation est gelée (finie et non bouclée) */
	private boolean frozen;

	/** Index de la frame courante */
	float frameIndex;

	/** Index de la frame courante */
	private float fps;

	/**
	 * Constructeur d'une animation.
	 *
	 * @param images       tableaux des images de l'animation
	 * @param frameDelayMs délai entre les frames (non utilisé directement ici)
	 * @param loop         indique si l'animation doit boucler
	 * @param owner        personnage propriétaire de l'animation
	 */
	public Animation(BufferedImage[] images, int frameNumber, boolean loop, float fps,
			AbstractCharacter<CharacterMode> owner) {
		this.images = images;
		this.shadows = null;
		this.frameNumber = frameNumber;
		this.frameNumberMid = frameNumber / 2;
		this.loop = loop;
		this.fps = fps;
		this.frozen = false;
		Random rand = new Random();
		System.out.println(images);
		this.frameIndex = rand.nextInt(images.length);
	}

	/**
	 * Constructeur d'une animation.
	 *
	 * @param images       tableaux des images de l'animation
	 * @param shadows      tableaux des images d'ombre correspondantes
	 * @param frameDelayMs délai entre les frames (non utilisé directement ici)
	 * @param loop         indique si l'animation doit boucler
	 * @param owner        personnage propriétaire de l'animation
	 */
	public Animation(BufferedImage[] images, BufferedImage[] shadows, int frameNumber, boolean loop, float fps,
			AbstractCharacter<CharacterMode> owner) {
		this.images = images;
		this.shadows = shadows;
		this.frameNumber = frameNumber;
		this.frameNumberMid = frameNumber / 2;
		this.loop = loop;
		this.fps = fps;
		this.frozen = false;
		Random rand = new Random();
		this.frameIndex = rand.nextInt(images.length);
	}

	/**
	 * Avance l'animation d'une frame et déclenche les callbacks associés.
	 *
	 * <p>
	 * Les callbacks possibles : begin, mid, tick, loop, end.
	 * </p>
	 *
	 * @param owner personnage propriétaire de l'animation
	 */

	private int lastIntFrame = -1;

	@Override
	public void tick(AbstractCharacter<? extends CharacterMode> owner) {
		if (!frozen) {
			int currentIntFrame = Math.round(frameIndex);

			// callback tick sur frame entière
			if (currentIntFrame != lastIntFrame) {
				lastIntFrame = currentIntFrame;
				owner.callTickTrigger(frameIndex);
			}

			// callback begin
			if (currentIntFrame == 0) {
				owner.callBeginTrigger();
			}

			// callback mid
			if (currentIntFrame == frameNumberMid) {
				owner.callMidTrigger();
			}

			// callback end
			if (currentIntFrame == frameNumber-1) {
				owner.callEndTrigger();
			}

			frameIndex += fps;

			// gestion fin ou loop
			if (frameIndex >= frameNumber) {
				if (loop) {
					frameIndex = 0;
					lastIntFrame = -1; // reset pour tick
				} else {
					frameIndex = frameNumber - 1;
					frozen = true;
				}
			}


		}
	}

	/**
	 * Retourne l'image courante de l'animation.
	 *
	 * @return image actuelle
	 */
	@Override
	public BufferedImage getCurrentFrame() {
		return images[(int) frameIndex];
	}

	/**
	 * Retourne l'image d'ombre courante de l'animation.
	 *
	 * @return image d'ombre actuelle
	 */
	@Override
	public BufferedImage getCurrentShadow() {
		if (shadows == null)
			return null;
		return shadows[(int) frameIndex];
	}

	/**
	 * Réinitialise l'animation au début et la dégelée si elle était gelée.
	 */
	@Override
	public void reset() {
		frameIndex = 0;
		frozen = false;
	}

	/**
	 * Force l'animation à sa dernière frame et la gèle.
	 */
	@Override
	public void lastFrame() {
		frameIndex = images.length - 1;
		frozen = true;
	}

	public void setFrameRate(float fps) {
		this.fps = fps;
	}

}
