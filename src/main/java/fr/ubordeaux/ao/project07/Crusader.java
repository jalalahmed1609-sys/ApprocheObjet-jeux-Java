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
 * Représente le personnage jouable "Crusader" avec ses animations,
 * sons et comportements spécifiques (marcher, courir, sauter, attaquer, subir
 * des dégâts, etc.).
 * Étend {@link AbstractCharacter} et utilise {@link CharacterMode} pour gérer
 * les différents états du personnage.
 */
public class Crusader extends AbstractCharacter<CharacterMode> {

    private static final float DEFAULT_SCALE = 3.0f;
    private static final float OFFSETX = 0.0f;
    private static final float OFFSETY = 0.0f;
    private static final float DEFAULT_ALPHA = 1f;

    static protected Map<Pair<Float, Color>, BufferedImage[][][]> cachedImages = new HashMap<>(); // [mode][direction][frame]
    static protected Map<Pair<Float, Color>, BufferedImage[][][]> cachedShadow_images = new HashMap<>(); // [mode][direction][frame]

    public enum Mode implements CharacterMode {
        WALK("walk", 15, true, DEFAULT_FPS),
        BLOCK("block", 16, false, DEFAULT_FPS),
        RUN("run", 17, true, DEFAULT_FPS),
        JUMP("jump", 16, false, DEFAULT_FPS),
        GOTHIT("gothit", 13, false, DEFAULT_FPS),
        ATTACK("attack", 18, false, DEFAULT_FPS),
        DEATH("death", 9, false, DEFAULT_FPS),
        IDLE("idle", 16, true, DEFAULT_FPS),
        TURN_LEFT("idle", 16, true, 0.6f),
        TURN_RIGHT("idle", 16, true, 0.6f),
        WALK_LEFT("walk", 15, true, 0.6f),
        WALK_RIGHT("walk", 15, true, 0.6f);

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

    public enum Sound {
        CARPET, CARPET_SOFT,
        WOOD, WOOD_SOFT, JUMP, GOTHIT, ATTACK, DEATH;
    }

    private Mode currentMode;
    private Sound currentSound;
    private Mode savedMode;
    private Sound savedSound;
    private boolean soundsInitialized;

    public Crusader(UUID id, float scale, Color color) {
        super(id, Mode.values().length, OFFSETX, OFFSETY, true);
        init(new Pair<>(scale, color));
    }

    public Crusader(UUID id) {
        this(id, DEFAULT_SCALE, null);
    }

    public Crusader(float scale) {
        this(UUID.randomUUID(), scale, null);
    }

    public Crusader(UUID id, float scale) {
        this(id, scale, null);
    }

    public Crusader(float scale, Color color) {
        this(UUID.randomUUID(), scale, color);
    }

    public Crusader() {
        this(UUID.randomUUID(), DEFAULT_SCALE, null);
    }

    public BufferedImage[][][] getCachedImages(Pair<Float, Color> pair) {
        return cachedImages.get(pair);
    }

    public BufferedImage[][][] getCachedShadow_images(Pair<Float, Color> pair) {
        return cachedShadow_images.get(pair);
    }

    public void setCachedImages(Pair<Float, Color> pair, BufferedImage[][][] cachedImages) {
        Crusader.cachedImages.put(pair, cachedImages);
    }

    public void setCachedShadow_images(Pair<Float, Color> pair, BufferedImage[][][] cachedShadow_images) {
        Crusader.cachedShadow_images.put(pair, cachedShadow_images);
    }

    private void init(Pair<Float, Color> pair) {
        savedMode = currentMode = Mode.IDLE;
        currentSound = Sound.CARPET_SOFT;
        currentDirection = Direction.EAST;
        initSounds();
        initAnimations(pair);
    }

    private void initSounds() {
        if (!soundsInitialized) {
            SoundCache.loadIfAbsent("CARPET", "/kenney_impact-sounds/Audio/footstep_carpet_000.wav", 0.8f);
            SoundCache.loadIfAbsent("CARPET_SOFT", "/kenney_impact-sounds/Audio/footstep_carpet_000.wav", 0.2f);
            SoundCache.loadIfAbsent("WOOD", "/kenney_impact-sounds/Audio/footstep_wood_000.wav", 0.8f);
            SoundCache.loadIfAbsent("WOOD_SOFT", "/kenney_impact-sounds/Audio/footstep_wood_000.wav", 0.2f);
            SoundCache.loadIfAbsent("JUMP", "/kenney_impact-sounds/Audio/footstep_carpet_000.wav", 0.8f);
            SoundCache.loadIfAbsent("GOTHIT", "/kenney_impact-sounds/Audio/impactTin_medium_000.wav", 0.8f);
            SoundCache.loadIfAbsent("ATTACK", "/kenney_impact-sounds/Audio/impactPlate_heavy_000.wav", 0.8f);
            SoundCache.loadIfAbsent("DEATH", "/kenney_impact-sounds/Audio/impactWood_heavy_000.wav", 0.8f);

            soundsInitialized = true; // pour éviter de recharger à chaque Crusader
        }
    }

    protected void loadImages(Pair<Float, Color> pair) {
        Mode[] modes = Mode.values();
        for (Mode mode : modes) {
            switch (mode) {
                case ATTACK:
                case DEATH:
                case GOTHIT:
                case IDLE:
                case JUMP:
                case RUN:
                case WALK:
                case BLOCK:
                {
                    int numFrames = mode.getNumFrames();
                    for (Direction direction : Direction.values()) {

                        BufferedImage[] frames = ImageLoader
                                .loadAnimations(
                                        numFrames,
                                        direction.ordinal(),
                                        String.format("/isometric-Mini-Crusader/%s/crusader_%s_",
                                                mode.getName(),
                                                mode.getName()),
                                        pair.first,
                                        1f,
                                        pair.second);

                        images[mode.ordinal()][direction.ordinal()] = frames;

                        shadow_images[mode.ordinal()][direction.ordinal()] = ImageLoader.loadAnimations(
                                numFrames,
                                direction.ordinal(),
                                String.format("/isometric-Mini-Crusader/%s/_shadows/shadow-crusader_%s_",
                                        mode.getName(),
                                        mode.getName()),
                                pair.first,
                                1f,
                                null);

                    }
                }

                    break;
                case TURN_LEFT:
                case TURN_RIGHT:
                case WALK_LEFT:
                case WALK_RIGHT: {
                    boolean left = switch (mode) {
                        case TURN_LEFT -> true;
                        case WALK_LEFT -> true;
                        default -> false;
                    };
                    int numFrames = mode.getNumFrames();
                    images[mode.ordinal()][0] = ImageLoader
                            .loadTurnAnimations(numFrames,
                                    8,
                                    String.format("/isometric-Mini-Crusader/%s/crusader_%s_",
                                            mode.getName(),
                                            mode.getName()),
                                    pair.first, 1f, null, left);

                    shadow_images[mode.ordinal()][0] = ImageLoader.loadTurnAnimations(
                            numFrames, 8,
                            String.format("/isometric-Mini-Crusader/%s/_shadows/shadow-crusader_%s_",
                                    mode.getName(),
                                    mode.getName()),
                            pair.first, 1f, null, left);

                }
                    break;
                default:
                    break;
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void loadAnimations() {
        Mode[] modes = Mode.values();
        for (Mode mode : modes) {
            switch (mode) {
                case ATTACK:
                case DEATH:
                case GOTHIT:
                case IDLE:
                case JUMP:
                case RUN:
                case WALK:
                case BLOCK:
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
                case TURN_RIGHT:
                case WALK_LEFT:
                case WALK_RIGHT: {
                        animations[mode.ordinal()][0] = new Animation(
                                images[mode.ordinal()][0],
                                shadow_images[mode.ordinal()][0],
                                mode.numFrames * 8,
                                mode.isLoop(), mode.getFps(), this);
                }
                    break;
                default:
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
        savedMode = currentMode;
        Mode newMode = (Mode) mode;
        currentMode = newMode;
        savedSound = currentSound;
        switch (mode) {
            case Mode.WALK -> currentSound = Sound.CARPET_SOFT;
            case Mode.RUN -> currentSound = Sound.CARPET;
            case Mode.ATTACK -> currentSound = Sound.ATTACK;
            case Mode.JUMP -> currentSound = Sound.JUMP;
            case Mode.GOTHIT -> currentSound = Sound.GOTHIT;
            case Mode.DEATH -> currentSound = Sound.DEATH;
            case Mode.TURN_LEFT -> currentDirection = Direction.values()[0];
            case Mode.TURN_RIGHT -> currentDirection = Direction.values()[0];
            case Mode.WALK_LEFT -> currentDirection = Direction.values()[0];
            case Mode.WALK_RIGHT -> currentDirection = Direction.values()[0];
            default -> {}
            }
        animations[currentMode.ordinal()][currentDirection.ordinal()].reset();
        }

    @Override
    public void setMode(String modeName) {
        setMode(Mode.valueOf(modeName));
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
    public boolean callTickTrigger(float frameIndex) {
        if (super.callTickTrigger(frameIndex)) {
            switch (currentMode) {
                case RUN, WALK -> {
                    if ((int) (frameIndex % 8) == 3) {
                        SoundCache.playCached(currentSound.name());
                    }
                }
                default -> {
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean callBeginTrigger() {
        if (super.callBeginTrigger()) {
            switch (currentMode) {
                case GOTHIT:
                case JUMP:
                    SoundCache.playCached(currentSound.name());
                default:
                    break;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean callMidTrigger() {
        return super.callMidTrigger();
    }

    @Override
    public boolean callEndTrigger() {
        if (super.callEndTrigger()) {
            switch (currentMode) {
                case ATTACK:
                case GOTHIT:
                    SoundCache.playCached(currentSound.name());
                    currentMode = savedMode;
                    currentSound = savedSound;
                    break;
                case JUMP:
                    currentMode = savedMode;
                    SoundCache.playCached(currentSound.name());
                    currentSound = savedSound;
                default:
                    break;
            }
            return true;
        }
        return false;
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
        Crusader other = (Crusader) obj;
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

    public Direction getDirection() {
        return currentDirection;
    }

}
