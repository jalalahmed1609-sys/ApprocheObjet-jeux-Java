package fr.ubordeaux.ao.project07.engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

public class ImageLoader {

	private static final int CROP_MAXSIZE = 100;

	public static BufferedImage loadImage(String path) {
		try {
			// 1️⃣ Essayer comme ressource du classpath
			var resource = ImageLoader.class.getResource(path);
			if (resource != null) {
				return ImageIO.read(resource);
			}

			// 2️⃣ Sinon, essayer comme fichier sur disque
			File file = new File(path);
			if (file.exists()) {
				return ImageIO.read(file);
			}
			System.err.println("Image not found: " + path + " (neither in resources nor on disk)");

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static BufferedImage loadImageIfExists(String path) {
		try {
			java.net.URL resource = ImageLoader.class.getResource(path);
			if (resource == null) {
				return null; // le fichier n'existe pas
			}
			return ImageIO.read(resource);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static BufferedImage scaleImageY(BufferedImage img, float scaleY) {
		int newHeight = Math.max(1, (int) (img.getHeight() * scaleY));
		BufferedImage scaled = new BufferedImage(img.getWidth(), newHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = scaled.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.drawImage(img, 0, 0, img.getWidth(), newHeight, null);
		g2d.dispose();
		return scaled;
	}

	/**
	 * Charge une série d'images selon le pattern :
	 * crusader_run_x00Y.png
	 * x = 1,3,5,7 (directions)
	 * y = 00..16 (frames)
	 */
	public static BufferedImage[] loadAnimations(int numFrames, int direction, String prefix, float scale,
			float scaleY, Color color) {
		BufferedImage[] animations = new BufferedImage[numFrames];
		final int MAX_PIXELS = 65536; // définition max : largeur * hauteur

		for (int frame = 0; frame < numFrames; frame++) {
			String frameNumber = String.format("%04d", frame);
			String directionNumber = String.format("%01d", direction);
			String path = String.format("%s%s%s.png", prefix, directionNumber, frameNumber);

			// System.out.println(path);

			BufferedImage img = loadImage(path);
			if (img != null) {
				int originalWidth = img.getWidth();
				int originalHeight = img.getHeight();

				// Application du scale
				int newWidth = (int) (originalWidth / scale);
				int newHeight = (int) (originalHeight / scale);

				// Limite de définition (nombre total de pixels)
				long pixels = (long) newWidth * newHeight;
				if (pixels > MAX_PIXELS) {
					double ratio = Math.sqrt((double) MAX_PIXELS / pixels);
					newWidth = Math.max(1, (int) Math.round(newWidth * ratio));
					newHeight = Math.max(1, (int) Math.round(newHeight * ratio));
				}

				BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = scaled.createGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2d.drawImage(img, 0, 0, newWidth, newHeight, null);
				g2d.dispose();

				if (color != null) {
					scaled = tintByLuminosity(scaled, color);
				}

				if (scaleY != 1f) {
					scaled = scaleImageY(scaled, scaleY);
				}

				animations[frame] = scaled;
			}
		}

		return animations;
	}

	/**
	 * Charge une série d'images selon le pattern :
	 * crusader_run_x00Y.png
	 * x = 1,3,5,7 (directions)
	 * y = 00..16 (frames)
	 */
	public static BufferedImage[] loadTurnAnimations(int numFrames, int numDirections, String prefix, float scale,
			float scaleY, Color color, boolean left) {
		final int MAX_PIXELS = 65536;

		// si numFrames == 8 directions par frame
		// int totalFrames = numFrames * NUM_DIRECTIONS;
		BufferedImage[] animations = new BufferedImage[numDirections * numFrames];

		// int index = 0;
		for (int index = 0; index < numDirections * numFrames; index++) {
			int dir = index % 8;
			int frame = index % numFrames;
			// for (int dir = 0; dir < numDirections; dir++) {
			// for (int frame = 0; frame < numFrames; frame++) {
			String directionNumber = String.format("%01d", left ? dir : numDirections - dir - 1);
			String frameNumber = String.format("%04d", frame);
			String path = String.format("%s%s%s.png", prefix, directionNumber, frameNumber);

			// System.out.println("###" + path);

			BufferedImage img = loadImage(path);

			if (img != null) {
				int originalWidth = img.getWidth();
				int originalHeight = img.getHeight();

				// Application du scale
				int newWidth = (int) (originalWidth / scale);
				int newHeight = (int) (originalHeight / scale);

				// Limite de définition (nombre total de pixels)
				long pixels = (long) newWidth * newHeight;
				if (pixels > MAX_PIXELS) {
					double ratio = Math.sqrt((double) MAX_PIXELS / pixels);
					newWidth = Math.max(1, (int) Math.round(newWidth * ratio));
					newHeight = Math.max(1, (int) Math.round(newHeight * ratio));
				}

				BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = scaled.createGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2d.drawImage(img, 0, 0, newWidth, newHeight, null);
				g2d.dispose();

				if (color != null) {
					scaled = tintByLuminosity(scaled, color);
				}

				if (scaleY != 1f) {
					scaled = scaleImageY(scaled, scaleY);
				}

				animations[index] = scaled;
			}

			// }
		}

		return animations;
	}

	/**
	 * Charge une série d'images selon le pattern :
	 * x_farmer.png
	 * x = 1,3,5,7 (directions)
	 */
	public static BufferedImage[] loadCropAnimations(int numFrames, String path, float scale,
			float scaleY, Color color) {
		BufferedImage[] animations = new BufferedImage[numFrames];
		final int MAX_PIXELS = 65536; // définition max : largeur * hauteur

		System.out.println("#" + path);

		BufferedImage img = loadImage(path);
		if (img != null) {

			int width = img.getWidth() / numFrames;
			int height = img.getHeight();
			int maxCols = numFrames;
			int maxRows = 1;
			int totalImages = Math.min(CROP_MAXSIZE, maxCols * maxRows);

			for (int i = 0; i < totalImages; i++) {

				int col = i % maxCols;
				int row = i / maxCols;

				int sx = col * width;
				int sy = row * height;

				// Taille réelle de la sous-image (pour les sprites plus petits)
				int subWidth = Math.min(width, img.getWidth() - sx);
				int subHeight = Math.min(height, img.getHeight() - sy);

				if (subWidth <= 0 || subHeight <= 0)
					continue;

				BufferedImage sub = img.getSubimage(sx, sy, subWidth, subHeight);

				// Image finale centrée dans la zone width x height + décalage offsetX/Y
				BufferedImage fixed = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = fixed.createGraphics();
				g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
						java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

				// Décalage pour centrer + offset
				int dx = (width - subWidth) / 2;
				int dy = (height - subHeight) / 2;

				g.drawImage(sub, dx, dy, subWidth, subHeight, null);
				g.dispose();

				// Dimensions après scale
				int scaledWidth = (int) (TileMap.tileWidth * scale);
				int scaledHeight = (int) (TileMap.tileHeight * 2 * scale);

				BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = scaled.createGraphics();
				g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
						java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				g2d.drawImage(fixed, 0, 0, scaledWidth, scaledHeight, null);
				g2d.dispose();

				if (color != null) {
					scaled = tintByLuminosity(scaled, color);
				}

				if (scaleY != 1f) {
					scaled = scaleImageY(scaled, scaleY);
				}


				animations[i] = scaled;
			}
		}
		return animations;
	}

	/**
	 * Mélange une couleur sur tous les pixels opaques, proportionnellement à leur
	 * luminosité.
	 */
	private static BufferedImage tintByLuminosity(BufferedImage img, Color targetColor) {
		int width = img.getWidth();
		int height = img.getHeight();
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		int rT = targetColor.getRed();
		int gT = targetColor.getGreen();
		int bT = targetColor.getBlue();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int argb = img.getRGB(x, y);
				int alpha = (argb >> 24) & 0xff;

				// Ne jamais toucher les pixels totalement transparents
				if (alpha == 0) {
					result.setRGB(x, y, argb);
					continue;
				}

				int red = (argb >> 16) & 0xff;
				int green = (argb >> 8) & 0xff;
				int blue = argb & 0xff;

				// Luminosité relative
				float lum = (0.299f * red + 0.587f * green + 0.114f * blue) / 255f;

				// On applique la couleur proportionnellement à la luminosité
				float factor = lum; // 0 (noir) -> 1 (blanc)
				red = (int) (red * (1 - factor) + rT * factor);
				green = (int) (green * (1 - factor) + gT * factor);
				blue = (int) (blue * (1 - factor) + bT * factor);

				red = Math.min(255, Math.max(0, red));
				green = Math.min(255, Math.max(0, green));
				blue = Math.min(255, Math.max(0, blue));

				result.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
			}
		}

		return result;
	}

	static void cropImageToImageMap(Map<Integer, ImageSceneRenderable> codeToImageMap, BufferedImage sheet, int width,
			int height, int offset, double scale, int offsetX, int offsetY,
			RenderType renderType, float iso) {

		int maxCols = sheet.getWidth() / width;
		int maxRows = sheet.getHeight() / height;
		int totalImages = Math.min(CROP_MAXSIZE, maxCols * maxRows);

		for (int i = 0; i < totalImages; i++) {

			int col = i % maxCols;
			int row = i / maxCols;

			int sx = col * width;
			int sy = row * height;

			// Taille réelle de la sous-image (pour les sprites plus petits)
			int subWidth = Math.min(width, sheet.getWidth() - sx);
			int subHeight = Math.min(height, sheet.getHeight() - sy);

			if (subWidth <= 0 || subHeight <= 0)
				continue;

			BufferedImage sub = sheet.getSubimage(sx, sy, subWidth, subHeight);

			// Image finale centrée dans la zone width x height + décalage offsetX/Y
			BufferedImage fixed = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = fixed.createGraphics();
			g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
					java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

			// Décalage pour centrer + offset
			int dx = (width - subWidth) / 2 + offsetX;
			int dy = (height - subHeight) / 2 + offsetY;

			g.drawImage(sub, dx, dy, subWidth, subHeight, null);
			g.dispose();

			// Dimensions après scale
			int scaledWidth = (int) (TileMap.tileWidth * scale);
			int scaledHeight = (int) (TileMap.tileHeight * 2 * scale);

			BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = scaled.createGraphics();
			g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
					java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g2d.drawImage(fixed, 0, 0, scaledWidth, scaledHeight, null);
			g2d.dispose();

			// === 1️⃣ Image normale ===
			codeToImageMap.put(i + offset, new ImageSceneRenderable(scaled, renderType, iso));

			// === 2️⃣ Image sombre (50%) ===
			BufferedImage dark50 = applyBrightness(scaled, 0.5f);
			codeToImageMap.put(i + offset + 10000, new ImageSceneRenderable(dark50, renderType, iso));

			// === 3️⃣ Image très sombre (20%) ===
			BufferedImage dark20 = applyBrightness(scaled, 0.2f);
			codeToImageMap.put(i + offset + 20000, new ImageSceneRenderable(dark20, renderType, iso));
		}
	}

	/**
	 * Applique un facteur de luminosité à une image.
	 * brightness = 1.0 → normal, 0.0 → noir
	 */
	private static BufferedImage applyBrightness(BufferedImage src, float brightness) {
		BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		RescaleOp op = new RescaleOp(
				new float[] { brightness, brightness, brightness, 1f }, // ne modifie pas alpha
				new float[] { 0f, 0f, 0f, 0f },
				null);
		op.filter(src, result);
		return result;
	}

	public static void loadImagesSheet(Map<Integer, ImageSceneRenderable> codeToImageMap, String path, int width,
			int height, float scale, int offsetX, int offsetY, int offset) {
		BufferedImage imagesSheet = ImageLoader.loadImage(path);
		if (imagesSheet != null) {
			ImageLoader.cropImageToImageMap(codeToImageMap, imagesSheet, width, height, offset, scale, offsetX, offsetY,
					RenderType.ITEM, 0);
		}
	}

}
