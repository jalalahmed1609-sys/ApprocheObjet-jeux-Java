package fr.ubordeaux.ao.project07.engine;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Loader {
	
	private static Loader instance;
	
	private Loader() {}
	
	public static Loader getInstance() {
		if (instance == null) {
			instance = new Loader();
		}
		return instance;
	}

	public static BufferedImage loadImage(String path) {
		try {
			return ImageIO.read(Loader.class.getResource(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Charge une série d'images selon le pattern :
	 * crusader_run_x00Y.png
	 * x = 1,3,5,7 (directions)
	 * y = 00..16 (frames)
	 */
	public static BufferedImage[] loadAnimations(int numFrames, int direction, String prefix, float scale) {
		BufferedImage[] animations = new BufferedImage[numFrames];

		for (int frame = 0; frame < numFrames; frame++) {
			String frameNumber = String.format("%04d", frame); // 0000,0001,...
			String directionNumber = String.format("%01d", direction);
			String path = String.format("%s%s%s.png", prefix, directionNumber, frameNumber);
			// System.err.println(path);

			BufferedImage img = loadImage(path);
			if (img != null) {
				// Scale l'image
				int newWidth = (int) (img.getWidth() / scale);
				int newHeight = (int) (img.getHeight() / scale);
				BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = scaled.createGraphics();
				g2d.drawImage(img, 0, 0, newWidth, newHeight, null);
				g2d.dispose();
				animations[frame] = scaled;
			}
		}

		return animations; // tableau d'images réduites

	}
}
