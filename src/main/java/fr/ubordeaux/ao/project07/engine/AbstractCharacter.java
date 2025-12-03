package fr.ubordeaux.ao.project07.engine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Classe abstraite représentant un personnage dans le jeu.
 * 
 * Cette classe gère les informations de position, de vitesse,
 * d'animations et de direction du personnage. Elle fournit
 * également des méthodes de rendu et des hooks pour les triggers
 * (début, fin, milieu, tick) lors de l'animation.
 * 
 *
 * @param <T> le type d'état du personnage, qui doit implémenter
 *            {@link CharacterMode}
 */
public abstract class AbstractCharacter<T extends CharacterMode> extends AbstractRenderable implements ICharacter<T> {

    protected static float DEFAULT_FPS = 1f;

    protected BufferedImage[][][] images; // [mode][direction][frame]
    protected BufferedImage[][][] shadow_images; // [mode][direction][frame]
    protected Animation<T>[][] animations; // [mode][direction]

    protected Direction currentDirection;
    protected float scale;
    protected UUID id;
    protected float _x, y, z;
    protected boolean shadow;
    protected float shadowX, shadowY, shadowZ;
    protected float offsetX, offsetY;
    protected float brightness;

    private Predicate<ICharacter<?>> beginAnimationTrigger;
    private Predicate<ICharacter<?>> midAnimationTrigger;
    private Predicate<ICharacter<?>> endAnimationTrigger;
    private Predicate<ICharacter<?>> tickAnimationTrigger;

    private Color color;

    @SuppressWarnings("unchecked")
    public AbstractCharacter(UUID id, int numberModes, float offsetX, float offsetY, boolean shadow) {
        this.id = id;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.shadow = shadow;
        images = new BufferedImage[numberModes][Direction.values().length][];
        if (shadow) {
            shadow_images = new BufferedImage[numberModes][Direction.values().length][];
        }
        animations = new Animation[numberModes][Direction.values().length];
        currentDirection = Direction.SOUTH;
    }

    public Animation<T> getCurrentAnimation(int mode) {
        return animations[mode][currentDirection.ordinal()];
    }

    protected void initAnimations(Pair<Float, Color> pair) {
        if (getCachedImages(pair) == null) {
            loadImages(pair);
            setCachedImages(pair, images);
            if (shadow) {
                setCachedShadow_images(pair, shadow_images);
            }
        } else {
            images = getCachedImages(pair);
            if (shadow) {
                shadow_images = getCachedShadow_images(pair);
            }
        }
        loadAnimations();
    }

    protected abstract BufferedImage[][][] getCachedShadow_images(Pair<Float, Color> pair);

    protected abstract BufferedImage[][][] getCachedImages(Pair<Float, Color> pair);

    protected abstract void setCachedShadow_images(Pair<Float, Color> pair, BufferedImage[][][] shadow_images);

    protected abstract void setCachedImages(Pair<Float, Color> pair, BufferedImage[][][] images);

    protected abstract void loadAnimations();

    protected abstract void loadImages(Pair<Float, Color> pair);

    public void render(Graphics g, int mode) {
        Animation<T> anim = getCurrentAnimation(mode);
        BufferedImage image = anim.getCurrentFrame();
        TileMap.drawImage(_x, y, z, image, g, getRenderType(), 1f, brightness);
    }

    public abstract RenderType getRenderType();

    // Déplace le personnage
    protected void setPosition(float x, float y, float z, float offsetX, float offsetY) {
        this._x = x + offsetX;
        this.y = y + offsetY;
        this.z = z;
    }

    // Déplace l'ombre du personnage
    protected void setShadowPosition(float x, float y, float z, float offsetX, float offsetY) {
        this.shadowX = x + offsetX;
        this.shadowY = y + offsetY;
        this.shadowZ = z;
    }

    // Déplace le personnage vers une case et ajuste la direction progressivement
    abstract public void setPosition(float x, float y, float z);

    // Déplace le personnage vers une case et ajuste la direction progressivement
    abstract public void setShadowPosition(float x, float y, float z);

    void turnPlayer(Direction targetDirection, int delayMs, Runnable onFinished) {

        Direction direction = currentDirection;
        int currentAngle = direction.getAngle();
        int targetAngle = targetDirection.getAngle();

        int delta = ((targetAngle - currentAngle + 540) % 360) - 180;
        int steps = Math.abs(delta) / 45;
        int stepSign = (delta >= 0) ? 1 : -1;

        if (steps == 0) {
            if (null != onFinished)
                onFinished.run();
            return;
        }

        javax.swing.Timer rotationTimer = new javax.swing.Timer(delayMs, null);
        final int[] stepCount = { 0 };

        rotationTimer.addActionListener(_ -> {
            int newAngle = (currentAngle + stepSign * 45 * stepCount[0] + 360) % 360;

            // Choisir la direction la plus proche
            Direction nearest = Direction.values()[0];
            int minDiff = 360;
            for (Direction dir : Direction.values()) {
                int diff = Math.abs(((dir.getAngle() - newAngle + 180) % 360) - 180);
                if (diff < minDiff) {
                    minDiff = diff;
                    nearest = dir;
                }
            }
            this.currentDirection = nearest;

            stepCount[0]++;
            if (stepCount[0] > steps) {
                setDirection(targetDirection);
                rotationTimer.stop();
                if (onFinished != null)
                    onFinished.run();
            }
        });

        rotationTimer.setRepeats(true);
        rotationTimer.start();
    }

    public void setX(float x) {
        this._x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public float getIsoDepth() {
        return _x - offsetX + y - offsetY;
    }

    public boolean callTickTrigger(float frameIndex) {
        if (tickAnimationTrigger != null) {
            return tickAnimationTrigger.test(this);
        }
        return true;
    }

    public boolean callBeginTrigger() {
        if (beginAnimationTrigger != null) {
            return beginAnimationTrigger.test(this);
        }
        return true;
    }

    public boolean callMidTrigger() {
        if (midAnimationTrigger != null) {
            return midAnimationTrigger.test(this);
        }
        return true;
    }

    public boolean callEndTrigger() {
        if (endAnimationTrigger != null) {
            return endAnimationTrigger.test(this);
        }
        return true;
    }

    @Override
    public void setTickAnimationTrigger(Predicate<ICharacter<?>> callback) {
        tickAnimationTrigger = callback;
    }

    @Override
    public void setBeginAnimationTrigger(Predicate<ICharacter<?>> callback) {
        beginAnimationTrigger = callback;
    }

    @Override
    public void setMidAnimationTrigger(Predicate<ICharacter<?>> callback) {
        midAnimationTrigger = callback;
    }

    @Override
    public void setEndAnimationTrigger(Predicate<ICharacter<?>> callback) {
        endAnimationTrigger = callback;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
        Pair<Float, Color> pair = new Pair<Float, Color>(scale, color);
        loadImages(pair);
        initAnimations(pair);
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
        Pair<Float, Color> pair = new Pair<Float, Color>(scale, color);
        loadImages(pair);
        initAnimations(pair);
    }

    @Override
    protected float getX() {
        return _x;
    }

    @Override
    protected float getY() {
        return y;
    }

    @Override
    protected float getZ() {
        return z;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public void setFrameRate(T mode, float fps) {

        for (Direction d : Direction.values()) {
            animations[mode.ordinal()][d.ordinal()].setFrameRate(fps);
        }
    }

    @Override
    public void setDirection(Direction newDirection) {
        currentDirection = newDirection;
    }

    @Override
    public void setDirection(String directinName) {
        currentDirection = Direction.valueOf(directinName);
    }

    @Override
    public void setDirection(Direction newDirection, int time) {
        turnPlayer(newDirection, time, () -> {
            currentDirection = newDirection;

            // Calculer la direction unitaire pour X, Y et Z selon la direction
            float dirX = 0, dirY = 0;

            switch (newDirection) {
                case NORTH -> {
                    dirX = 0;
                    dirY = -1;
                }
                case SOUTH -> {
                    dirX = 0;
                    dirY = 1;
                }
                case EAST -> {
                    dirX = 1;
                    dirY = 0;
                }
                case WEST -> {
                    dirX = -1;
                    dirY = 0;
                }

                case NORTHEAST -> {
                    dirX = 1;
                    dirY = -1;
                }
                case NORTHWEST -> {
                    dirX = -1;
                    dirY = -1;
                }
                case SOUTHEAST -> {
                    dirX = 1;
                    dirY = 1;
                }
                case SOUTHWEST -> {
                    dirX = -1;
                    dirY = 1;
                }

            }

            // Normaliser si diagonale dans XY
            if (dirX != 0 && dirY != 0) {
                float invSqrt2 = 1 / (float) Math.sqrt(2);
                dirX *= invSqrt2;
                dirY *= invSqrt2;
            }

        });
    }

    @Override
    public void setOffset(float x, float y) {
        this.offsetX = x;
        this.offsetY = y;
    }

    public float getFrameIndex(int mode) {
        Animation<T> anim = getCurrentAnimation(mode);
        return anim.frameIndex;
    }

    @Override
    public float getFrameIndex() {
        return getFrameIndex(getCurrentMode().ordinal());
    }

    @Override
    public void resetAnimation() {
        Animation<T> anim = getCurrentAnimation(getCurrentMode().ordinal());
        anim.reset();
    }

    public abstract T getCurrentMode();

}
