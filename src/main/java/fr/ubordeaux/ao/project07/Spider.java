package fr.ubordeaux.ao.project07;

import java.awt.Color;
import fr.ubordeaux.ao.project07.engine.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
 
/**
 * Représente une araignée avec ses animations et comportements spécifiques.
 * Étend {@link AbstractCharacter} et utilise {@link CharacterMode} pour gérer
 * les modes.
 */
public class Spider extends AbstractCharacter<CharacterMode> {
    // Pattern Strategy pour l'attaque
    private fr.ubordeaux.ao.project07.patterns.AttackStrategy attackStrategy;

    private static final float DEFAULT_SCALE = 2.5f;
    private static final float OFFSETX = -0.1f;
    private static final float OFFSETY = 0.1f;
    private static final float DEFAULT_ALPHA = 1f;

    static protected Map<Pair<Float, Color>, BufferedImage[][][]> cachedImages = new HashMap<>(); // [mode][direction][frame]
    static protected Map<Pair<Float, Color>, BufferedImage[][][]> cachedShadow_images = new HashMap<>(); // [mode][direction][frame]

    public enum Mode implements CharacterMode {
        WALK("walk", 25, true, DEFAULT_FPS),
        IDLE("walk", 1, true, DEFAULT_FPS),
        ATTACK("attack", 8, false, DEFAULT_FPS),
        DEATH("death", 8, false, DEFAULT_FPS),
        TURN_LEFT("walk", 25, true, 0.6f),
        TURN_RIGHT("walk", 25, true, 0.6f);

        private final String name;
        private final int numFrames;
        private final boolean loop;
        private float fps;

        // Nouveau constructeur
        Mode(String name, int numFrames, boolean loop, float fps) {
            this.name = name;
            this.numFrames = numFrames;
            this.loop = loop;
            this.fps = fps;
        }

        public String getName() {
            return name;
        }

        @Override
        public int getNumFrames() {
            return numFrames;
        }

        @Override
        public boolean isLoop() {
            return loop;
        }

        @Override
        public float getFps() {
            return fps;
        }
    }

    private Mode currentMode;
    private Mode previousMode;
    protected float previousSpeedX, previousSpeedY, previousSpeedZ;
    private boolean soundsInitialized;

    public Spider(UUID id, float scale, Color color) {
        super(id, Mode.values().length, OFFSETX, OFFSETY, true);
        init(new Pair<>(scale, color));
        // Par défaut, attaque au corps à corps
        this.attackStrategy = new fr.ubordeaux.ao.project07.patterns.MeleeAttack();
    }

    public Spider(UUID id) {
        this(UUID.randomUUID(), DEFAULT_SCALE, null);
    }

    public Spider(UUID id, float scale) {
        this (id, scale, null);
    }

    public Spider(float scale) {
        this(UUID.randomUUID(), scale, null);
    }

    public Spider() {
        this(UUID.randomUUID(), DEFAULT_SCALE, null);
        this.attackStrategy = new fr.ubordeaux.ao.project07.patterns.MeleeAttack();
    }

    public BufferedImage[][][] getCachedImages(Pair<Float, Color> pair) {
        return cachedImages.get(pair);
    }

    public BufferedImage[][][] getCachedShadow_images(Pair<Float, Color> pair) {
        return cachedShadow_images.get(pair);
    }

    public void setCachedImages(Pair<Float, Color> pair, BufferedImage[][][] cachedImages) {
        Spider.cachedImages.put(pair, cachedImages);
    }

    public void setCachedShadow_images(Pair<Float, Color> pair, BufferedImage[][][] cachedShadow_images) {
        Spider.cachedShadow_images.put(pair, cachedShadow_images);
    }

    private void init(Pair<Float, Color> pair) {
        currentMode = Mode.WALK;
        currentDirection = Direction.EAST;
        initSounds();
        initAnimations(pair);
    }

    private void initSounds() {
        if (!soundsInitialized) {
            // Sons spécifiques à Spider
            SoundCache.loadIfAbsent("ATTACK", "/kenney_impact-sounds/Audio/footstep_snow_000.wav", 0.4f);
            SoundCache.loadIfAbsent("DEATH", "/kenney_impact-sounds/Audio/impactSoft_heavy_000.wav", 0.4f);

            // Tu peux ajouter d’autres sons si besoin
            soundsInitialized = true; // une seule fois pour toutes les instances
        }
    }

    @Override
    protected void loadImages(Pair<Float, Color> pair) {
        Mode[] modes = Mode.values();
        for (Mode mode : modes) {
            switch (mode) {
                case ATTACK:
                case DEATH:
                case IDLE:
                case WALK: {

                    int numFrames = mode.getNumFrames();
                    for (Direction direction : Direction.values()) {
                        int correctedDirection = (direction.ordinal() + 6) % 8;
                        images[mode.ordinal()][direction.ordinal()] = ImageLoader.loadAnimations(
                                numFrames,
                                correctedDirection,
                                String.format("/bw_spider/%s/%s-BW_Spider_",
                                        mode.getName(),
                                        mode.getName()),
                                pair.first,
                                1f,
                                pair.second);

                        shadow_images[mode.ordinal()][direction.ordinal()] = ImageLoader.loadAnimations(numFrames,
                                correctedDirection,
                                String.format("/bw_spider/%s/_shadows/%s-BW_Spider-shadow_",
                                        mode.getName(),
                                        mode.getName()),
                                pair.first,
                                1f,
                                null);

                    }
                }
                    break;
                case TURN_LEFT:
                case TURN_RIGHT: {
                    boolean left = switch (mode) {
                        case TURN_LEFT -> true;
                        default -> false;
                    };
                    int numFrames = mode.getNumFrames();
                    images[mode.ordinal()][0] = ImageLoader
                            .loadTurnAnimations(numFrames, 8,
                                    String.format("/bw_spider/%s/%s-BW_Spider_",
                                            mode.getName(),
                                            mode.getName()),
                                    pair.first, 1f, null, left);

                    shadow_images[mode.ordinal()][0] = ImageLoader.loadTurnAnimations(
                            numFrames, 8,
                            String.format("/bw_spider/%s/_shadows/%s-BW_Spider-shadow_",
                                    mode.getName(),
                                    mode.getName()),
                            pair.first, 1f, null, left);

                }
                    break;
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void loadAnimations() {
        Mode[] modes = Mode.values();
        for (Mode mode : modes) {
            switch (mode) {
                case ATTACK:
                case DEATH:
                case IDLE:
                case WALK:
                {
                    for (Direction direction : Direction.values()) {
                        animations[mode.ordinal()][direction.ordinal()] = new Animation(
                                images[mode.ordinal()][direction.ordinal()],
                                shadow_images[mode.ordinal()][direction.ordinal()],
                                mode.numFrames,
                                mode.isLoop(), mode.getFps(), this);
                    }
                }
                    break;
                case TURN_LEFT:
                case TURN_RIGHT: {
                        animations[mode.ordinal()][0] = new Animation(
                                images[mode.ordinal()][0],
                                shadow_images[mode.ordinal()][0],
                                mode.numFrames * 8,
                                mode.isLoop(), mode.getFps(), this);
                }
                    break;
            }
        }
    }

    @Override
    public Mode getCurrentMode() {
        return currentMode;
    }

    @Override
    public void setMode(CharacterMode mode) {
        Mode s = (Mode) mode;
        previousMode = currentMode;
        currentMode = s;
        switch (mode) {
            case Mode.TURN_LEFT -> currentDirection = Direction.values()[0];
            case Mode.TURN_RIGHT -> currentDirection = Direction.values()[0];
            default -> {
            }
        }
            animations[currentMode.ordinal()][currentDirection.ordinal()].reset();
    }

    @Override
    public void setMode(String modeName) {
        setMode(Mode.valueOf(modeName));
    }

    // Méthode pour changer la stratégie d'attaque
    public void setAttackStrategy(fr.ubordeaux.ao.project07.patterns.AttackStrategy strategy) {
        this.attackStrategy = strategy;
    }

    // Méthode pour attaquer
    public void attack() {
        if (attackStrategy != null) attackStrategy.attack();
    }

    public RenderType getRenderType() {
        return RenderType.CONSTRUCTION;
    }

    @Override
    public void render(Graphics g) {
        Animation<CharacterMode> anim = getCurrentAnimation(currentMode.ordinal());
        BufferedImage image = anim.getCurrentFrame();
        BufferedImage shadow = anim.getCurrentShadow();
        TileMap.drawImage(_x, y, z, image, g, RenderType.CONSTRUCTION, DEFAULT_ALPHA, brightness);
        TileMap.drawImage(shadowX, shadowY, shadowZ, shadow, g, RenderType.CONSTRUCTION, DEFAULT_ALPHA, brightness);

        super.render(g, currentMode.ordinal());
    }

    public int getRank() {
        return 1;
    }

    @Override
    public boolean callBeginTrigger() {
        return super.callEndTrigger();
    }

    @Override
    public boolean callEndTrigger() {
        if (super.callEndTrigger()) {
            switch (currentMode) {
                case ATTACK -> {
                    SoundCache.playCached(currentMode.name());
                    currentMode = previousMode;
                }
                default -> {
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean callTickTrigger(float frameIndex) {
        return super.callTickTrigger(frameIndex);
    }

    @Override
    protected float getZ() {
        return z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Spider other = (Spider) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public void setPosition(float x, float y, float z) {
        setPosition(x, y, z, offsetX, offsetY);
        setShadowPosition(x, y, z, offsetX, offsetY);
    }

    @Override
    public void setShadowPosition(float x, float y, float z) {
        setShadowPosition(x, y, z, offsetX, offsetY);
    }

}
