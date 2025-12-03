package fr.ubordeaux.ao.project07.engine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Représente le plateau de jeu isométrique et gère le rendu des tuiles, murs et
 * personnages.
 * 
 * Cette classe hérite de {@link JPanel} et implémente {@link IWindowGame}. Elle
 * gère :
 * 
 * - Le rendu des objets 3D et des personnages.
 * - Le fond du plateau et les textures des tuiles.
 * - Les animations des personnages.
 * - La gestion des positions et états du joueur.
 * 
 * 
 */
class BoardGame extends JPanel implements IWindowGame {

	private static final long serialVersionUID = 1L;

	/** Largeur d'une image à découper dans la spritesheet */
	private final static int CROP_WIDTH = 256;

	/** Hauteur d'une image à découper dans la spritesheet */
	private final static int CROP_HEIGHT = 512;

	int width;

	int height;

	/** Image de fond du plateau */
	private BufferedImage background;

	/** Mapping du code d’image vers son rang de rendu */
	private final Map<Integer, Integer> codeToRankMap = new HashMap<>();

	/** Liste des sols présents dans la scène */
	List<AbstractRenderable> grounds = new ArrayList<>();

	/** Liste des objets 3D présents dans la scène */
	Set<ImageSceneRenderable> sceneObjects = new HashSet<>();

	/** Liste des personnages présents dans la scène */
	List<AbstractCharacter<? extends CharacterMode>> sceneCharacters = new ArrayList<>();

	/** Liste des personnages du plateau */
	@SuppressWarnings("rawtypes")
	private List<ICharacter> characters;

	/** Boucle de jeu pour mettre à jour les animations */
	private Timer gameLoop;

	/** Mapping du code d’image vers l’objet Image correspondant */
	private Map<Integer, ImageSceneRenderable> codeToImageMap;

	/** Référence vers le joueur */
	/**
	 * Constructeur principal.
	 * 
	 * @param width  largeur du plateau en pixels
	 * @param height hauteur du plateau en pixels
	 */
	public BoardGame(int width, int height, int tileSize, int originX, int originY, int fps) {
		this.width = width;
		this.height = height;

		background = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		// Initialisation du TileMap pour le rendu
		TileMap.init(tileSize, originX, originY);
		characters = new ArrayList<>();
		codeToImageMap = new HashMap<>();

		initImagesSheet();

		// Initialisation des rangs de rendu des objets
		codeToRankMap.put(138, 0);
		codeToRankMap.put(136, 2);
		codeToRankMap.put(139, 2);
		codeToRankMap.put(137, 2);
		codeToRankMap.put(188, 0);
		codeToRankMap.put(189, 2);
		codeToRankMap.put(190, 2);
		codeToRankMap.put(191, 2);
		codeToRankMap.put(180, 0);
		codeToRankMap.put(181, 2);
		codeToRankMap.put(182, 2);
		codeToRankMap.put(183, 2);

		startGameLoop(fps);
	}

	public void startGameLoop(int fps) {
		if (gameLoop != null) {
			gameLoop.stop();
		}
		gameLoop = new Timer(1000 / fps, _ -> {
			updateAnimations();
		});
		gameLoop.start();
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		TileMap.originX = width / 2;
		TileMap.originY = height / 4;
		background = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	void initImagesSheet() {
		// Chargement et découpe des spritesheets
		// Grounds
		BufferedImage imagesSheet = ImageLoader.loadImage("/isometric-tiles/all_000.png");
		ImageLoader.cropImageToImageMap(codeToImageMap, imagesSheet, CROP_WIDTH, CROP_HEIGHT, 0, 1, 0, 0, RenderType.FLOOR,
				-1.5f);

		// Walls
		imagesSheet = ImageLoader.loadImage("/isometric-tiles/all_100.png");
		ImageLoader.cropImageToImageMap(codeToImageMap, imagesSheet, CROP_WIDTH, CROP_HEIGHT, 100, 1, 0, 0,
				RenderType.CONSTRUCTION, -0.5f);

		// Walls con't
		imagesSheet = ImageLoader.loadImage("/isometric-tiles/all_200.png");
		ImageLoader.cropImageToImageMap(codeToImageMap, imagesSheet, CROP_WIDTH, CROP_HEIGHT, 200, 1, 0, 0,
				RenderType.CONSTRUCTION, -0.5f);

		// Stairs
		imagesSheet = ImageLoader.loadImage("/isometric-tiles/all_300.png");
		ImageLoader.cropImageToImageMap(codeToImageMap, imagesSheet, CROP_WIDTH, CROP_HEIGHT, 300, 1, 0, 0,
				RenderType.CONSTRUCTION, 0);

		// Constructions
		imagesSheet = ImageLoader.loadImage("/isometric-tiles/all_400.png");
		ImageLoader.cropImageToImageMap(codeToImageMap, imagesSheet, CROP_WIDTH, CROP_HEIGHT, 400, 1, 0, 0,
				RenderType.CONSTRUCTION, 0);

		// Objects
		imagesSheet = ImageLoader.loadImage("/isometric-tiles/all_500.png");
		ImageLoader.cropImageToImageMap(codeToImageMap, imagesSheet, CROP_WIDTH, CROP_HEIGHT, 500, 1.4f, 0, 36, RenderType.ITEM,
				0);

	}

	/**
	 * Met à jour toutes les animations des personnages.
	 */
	private void updateAnimations() {
		@SuppressWarnings("rawtypes")
		List<ICharacter> charactersCopy;
		synchronized (characters) {
			charactersCopy = new ArrayList<>(characters);
		}

		for (Object c : charactersCopy) {
			@SuppressWarnings("unchecked")
			AbstractCharacter<CharacterMode> ac = (AbstractCharacter<CharacterMode>) c;
			CharacterMode mode = ac.getCurrentMode();
			if (mode == null)
				continue;

			Animation<CharacterMode> animation = ac.getCurrentAnimation(mode.ordinal());
			if (animation != null) {
				animation.tick(ac);
			}
		}
		repaint();
	}

	private void renderScene(Graphics g, List<AbstractRenderable> sceneObjects) {
		// Trier les objets par z, puis x+y, puis par type
		List<AbstractRenderable> sorted = sceneObjects.stream()
				.sorted(Comparator
						.comparingInt(o -> ((int) ((AbstractRenderable) o).getZ()))
						.thenComparingInt(o -> ((int) ((AbstractRenderable) o).getIsoDepth()))
						.thenComparingInt(o -> ((AbstractRenderable) o).getRenderType().ordinal())
						.thenComparingInt(o -> ((AbstractRenderable) o).getRank())
						.thenComparing(o -> ((AbstractRenderable) o).getIsoDepth()))
				.toList();

		for (AbstractRenderable obj : sorted) {
			obj.render(g);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (background != null) {
			g.drawImage(background, 0, 0, null);
		}

		List<AbstractRenderable> renderableScene = new ArrayList<>();

		// Créer des copies locales pour éviter la ConcurrentModificationException
		List<AbstractRenderable> groundsCopy;
		List<AbstractRenderable> sceneObjectsCopy;
		List<AbstractRenderable> sceneCharactersCopy;

		synchronized (grounds) {
			groundsCopy = new ArrayList<>(grounds);
		}
		synchronized (sceneObjects) {
			sceneObjectsCopy = new ArrayList<>(sceneObjects);
		}
		synchronized (sceneCharacters) {
			sceneCharactersCopy = new ArrayList<>(sceneCharacters);
		}

		for (AbstractRenderable ar : groundsCopy) {
			if (TileMap.distance(ar) < 2 * width) {
				renderableScene.add(ar);
			}
		}
		for (AbstractRenderable ar : sceneObjectsCopy) {
			if (TileMap.distance(ar) < 2 * width) {
				renderableScene.add(ar);
			}
		}
		for (AbstractRenderable ar : sceneCharactersCopy) {
			if (TileMap.distance(ar) < 2 * width) {
				renderableScene.add(ar);
			}
		}

		renderScene(g, renderableScene);
	}

	/**
	 * Remplit le plateau avec un fond en damier.
	 */
	@Override
	public void fillCheckerboardBackground(int x, int y, int width, int height) {

		// background = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = background.createGraphics();

		// dessiner la grille à partir des codes (exemple avec CODEBG = 72)
		for (int _y = y; _y < y + height; _y++) {
			for (int _x = x; _x < x + width; _x++) {
				if ((Math.abs(_x) + Math.abs(_y)) % 2 == 0) {
					TileMap.drawTile(_x, _y, 0, g, Color.BLACK);
				} else {
					TileMap.drawTile(_x, _y, 0, g, Color.WHITE);
				}
			}
		}
		g.dispose();
	}

	/**
	 * Remplit le plateau avec une image de tuile spécifique.
	 * 
	 * @param code  code de la tuile
	 * @param alpha transparence de la tuile
	 */
	@Override
	public void fillArea(int code, int x, int y, int z, int width, int height, float alpha, float brightness) {

		Graphics2D g = background.createGraphics();

		int XMINBG = x;
		int XMAXBG = x + width;
		int YMINBG = y;
		int YMAXBG = y + height;

		ImageSceneRenderable tileImg = codeToImageMap.get(code);
		if (tileImg != null) {
			for (int _y = YMINBG; _y < YMAXBG; _y++) {
				for (int _x = XMINBG; _x < XMAXBG; _x++) {

					if (code != -1) {
						ImageSceneRenderable image = codeToImageMap.get(code);
						int rank = codeToRank(code);
						if (image != null) {
							ImageSceneRenderable imageRendarable3D = new ImageSceneRenderable(
									code, _x, _y, z,
									image.getBufferedImage(),
									rank,
									image.getRenderType(), image.getIso(), alpha);
							grounds.add(imageRendarable3D);
						}
					}
				}
			}
		}

		g.dispose();
	}

	// les murs sont W, S, N, E
	// W, S : rank = 0
	// N, E : rank = 2
	// characters : rank = 1 (entre les deux)
	private int codeToRank(int code) {
		if (codeToRankMap.keySet().contains(code)) {
			return codeToRankMap.get(code);
		} else {
			if (code < 100)
				return 0;
			if (code % 4 == 0 || code % 4 == 2) {
				return 0;
			} else {
				return 2;
			}
		}
	}

	/**
	 * Ajoute une tuile ou un objet à la scène.
	 * 
	 * @param code  code de l’objet
	 * @param x     coordonnée X
	 * @param y     coordonnée Y
	 * @param z     coordonnée Z
	 * @param alpha transparence
	 */
	@Override
	public void add(int code, int x, int y, int z, float alpha) {
		if (code != -1) {
			ImageSceneRenderable image = codeToImageMap.get(code);
			int rank = codeToRank(code);
			if (image != null) {
				ImageSceneRenderable imageRendarable3D = new ImageSceneRenderable(
						code, x, y, z,
						image.getBufferedImage(), rank,
						image.getRenderType(), image.getIso(), alpha);
				sceneObjects.add(imageRendarable3D);
			}
		}
	}

	/**
	 * Ajoute une matrice complète de tuiles à la scène.
	 * 
	 * @param matrix matrice 3D de codes de tuiles
	 * @param alpha  transparence
	 */
	@Override
	public void add(int[][][] matrix, float alpha) {
		for (int z = 0; z < matrix.length; z++) {
			for (int y = 0; y < matrix[z].length; y++) {
				for (int x = 0; x < matrix[z][y].length; x++) {

					int code = matrix[z][y][x];

					if (code != -1) {
						ImageSceneRenderable image = codeToImageMap.get(code);
						int rank = codeToRank(code);
						if (image != null) {
							ImageSceneRenderable imageRendarable3D = new ImageSceneRenderable(
									code, x, y, z,
									image.getBufferedImage(), rank,
									image.getRenderType(), image.getIso(), alpha);

							sceneObjects.add(imageRendarable3D);
						}

					}

				}
			}
		}
	}

	/**
	 * Ajoute un personnage à la scène.
	 * 
	 * @param character personnage à ajouter
	 */
	@Override
	public void add(ICharacter<? extends ICharacterMode> character) {
		characters.add(character);
		AbstractCharacter<? extends CharacterMode> ac = (AbstractCharacter<? extends ICharacterMode>) character;
		sceneCharacters.add(ac);
	}

	/**
	 * Vide la scène.
	 */
	@Override
	public void clear() {
		sceneObjects.clear();
	}

	@Override
	public void scroll(int x, int y) {
		throw new UnsupportedOperationException("Unimplemented method 'scroll'");
	}

	@Override
	public void addTileBackground(Color color, int x, int y, int z) {

		// Réutilise l'image existante
		Graphics2D g = background.createGraphics();

		// Dessine la tuile sur l'image actuelle
		TileMap.drawTile(x, y, z, g, color);

		g.dispose();
	}

	@Override
	public void addCubeBackground(Color color, int x, int y, int z) {

		// Réutilise l'image existante
		Graphics2D g = background.createGraphics();

		// Dessine la tuile sur l'image actuelle
		TileMap.drawCube(x, y, z, g, color);

		g.dispose();
	}

	@Override
	public void add(int code, int x, int y, int z) {
		add(code, x, y, z, 1f);
	}

	@Override
	public void setFPS(int fps) {
		throw new UnsupportedOperationException("Unimplemented method 'setFPS'");
	}

	@Override
	public void setTileSize(int tileSize) {
		throw new UnsupportedOperationException("Unimplemented method 'setTileSize'");
	}

	@Override
	public void fillArea(int code, int x, int y, int z, int width, int height) {
		throw new UnsupportedOperationException("Unimplemented method 'fillTilesBackground'");
	}

	@Override
	public void add(int[][][] matrix) {
		throw new UnsupportedOperationException("Unimplemented method 'add'");
	}

	@Override
	public Point2D getIsoCoordinatesFromScreen(int x, int y) {
		throw new UnsupportedOperationException("Unimplemented method 'getIsoCoordinatesFromScreen'");
	}

	@Override
	public void playSound(String soundId) {
		throw new UnsupportedOperationException("Unimplemented method 'playSound'");
	}

	@Override
	public void remove(int code, int x, int y, int z) {
		sceneObjects.remove(new ImageSceneRenderable(code, x, y, z));
	}

	@Override
	public void remove(ICharacter<? extends ICharacterMode> character) {
		sceneCharacters.remove(character);
	}

	@Override
	public void loadImagesSheet(String path, int width, int height, float scale, int offsetX, int offsetY, int offset) {
		ImageLoader.loadImagesSheet(codeToImageMap, path, width, height, scale, offsetX, offsetY, offset);
	}

	@Override
	public void addBorder(int borderTileId, int cornerTileId, int x, int y, int width, int height) {
		// Top and Bottom borders
		for (int i = x; i < x + width; i++) {
			add(borderTileId, i, y - 1, 0); // Top
			add(borderTileId, i, y + height, 0); // Bottom
		}
		
		// Left and Right borders
		for (int j = y; j < y + height; j++) {
			add(borderTileId, x - 1, j, 0); // Left
			add(borderTileId, x + width, j, 0); // Right
		}
		
		// Corners
		add(cornerTileId, x - 1, y - 1, 0); // Top-Left
		add(cornerTileId, x + width, y - 1, 0); // Top-Right
		add(cornerTileId, x - 1, y + height, 0); // Bottom-Left
		add(cornerTileId, x + width, y + height, 0); // Bottom-Right
	}
}
