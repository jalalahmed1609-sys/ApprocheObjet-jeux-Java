package fr.ubordeaux.ao.project07.engine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Fenêtre principale du jeu, gère le plateau isométrique.
 */
public class WindowGame extends JFrame implements IWindowGame {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = 665;
    private static final int DEFAULT_TILE_SIZE = 100;
    private static final int DEFAULT_FPS = 50;

    private final BoardGame boardGame;

    /**
     * Crée et affiche la fenêtre principale du jeu.
     * Toujours appelée sur l'Event Dispatch Thread via SwingUtilities.
     */
    public WindowGame() {
        super("Isometric 3D Game");

        // Création du panneau principal du jeu
        boardGame = new BoardGame(
                DEFAULT_WIDTH, DEFAULT_HEIGHT,
                DEFAULT_TILE_SIZE,
                DEFAULT_WIDTH / 2, DEFAULT_HEIGHT / 4,
                DEFAULT_FPS);

        // Définir le content pane
        setContentPane(boardGame);

        // Paramètres de la fenêtre
        setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        setVisible(true);
    }

    /**
     * Lance la fenêtre de jeu sur le thread graphique Swing.
     */
    public static void launch() {
        SwingUtilities.invokeLater(WindowGame::new);
    }

    // =============================
    // Implémentation IWindowGame
    // =============================

    @Override
    public void setTileSize(int tileSize) {
        TileMap.init(tileSize, TileMap.originX, TileMap.originY);
        boardGame.initImagesSheet();
    }

    @Override
    public void setFPS(int fps) {
        boardGame.startGameLoop(fps);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        boardGame.setSize(width, height);
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
    }

    @Override
    public void setBackground(Color color) {
        if (boardGame != null)
            boardGame.setBackground(color);
        super.setBackground(color);
    }

    @Override
    public void setResizable(boolean resizable) {
        super.setResizable(resizable);
    }

    // =============================
    // Délégation au BoardGame
    // =============================

    @Override
    public void fillArea(int code, int x, int y, int z, int width, int height, float alpha, float brightness) {
        boardGame.fillArea(code, x, y, z, width, height, alpha, brightness);
    }

    @Override
    public void fillArea(int code, int x, int y, int z, int width, int height) {
        boardGame.fillArea(code, x, y, z, width, height, 1f, 1f);
    }

    @Override
    public void fillCheckerboardBackground(int x, int y, int width, int height) {
        boardGame.fillCheckerboardBackground(x, y, width, height);
    }

    @Override
    public void add(ICharacter<? extends ICharacterMode> character) {
        boardGame.add(character);
    }

    @Override
    public void add(int code, int x, int y, int z, float alpha) {
        boardGame.add(code, x, y, z, alpha);
    }

    @Override
    public void add(int code, int x, int y, int z) {
        boardGame.add(code, x, y, z, 1f);
    }

    @Override
    public void add(int[][][] matrix) {
        boardGame.add(matrix, 1f);
    }

    @Override
    public void add(int[][][] matrix, float alpha) {
        boardGame.add(matrix, alpha);
    }

    @Override
    public void clear() {
        boardGame.clear();
    }

    @Override
    public void scroll(int x, int y) {
        TileMap.scroll(x, y);
    }

    @Override
    public void addTileBackground(Color color, int x, int y, int z) {
        boardGame.addTileBackground(color, x, y, z);
    }

    @Override
    public void addCubeBackground(Color color, int x, int y, int z) {
        boardGame.addCubeBackground(color, x, y, z);
    }

    @Override
    public void playSound(String soundId) {
        // Vérifie si le son est dans le cache
        if (!SoundCache.isCached(soundId)) {
            // Déduire le chemin automatiquement, par convention
            String path = "/sounds/" + soundId.toLowerCase() + ".wav";

            // Charger et mettre en cache
            SoundCache.loadIfAbsent(soundId, path, 1.0f); // volume par défaut
        }

        // Jouer le son (qu'il ait été chargé ou déjà présent)
        SoundCache.playCached(soundId);
    }

    @Override
    public Point2D getIsoCoordinatesFromScreen(int x, int y) {
        Point2D point = TileMap.screenToGrid(x, y);
        point.setLocation(point.getX() + TileMap.scrollX, point.getY() + TileMap.scrollY);
        return point;
    }

    @Override
    public void remove(int code, int x, int y, int z) {
        boardGame.remove(code, x, y, z);
    }

    @Override
    public void loadImagesSheet(String path, int width, int height, float scale, int offsetX, int offsetY, int offset) {
        boardGame.loadImagesSheet(path, width, height, scale, offsetX, offsetY, offset);
    }

    @Override
    public void remove(ICharacter<? extends ICharacterMode> character) {
        boardGame.remove(character);
    }

    @Override
    public void addBorder(int borderTileId, int cornerTileId, int x, int y, int width, int height) {
        boardGame.addBorder(borderTileId, cornerTileId, x, y, width, height);
    }

}
