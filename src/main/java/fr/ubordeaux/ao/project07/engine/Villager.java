package fr.ubordeaux.ao.project07.engine;
import fr.ubordeaux.ao.project07.Crusader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Villager extends AbstractCharacter<CharacterMode> {

    private static final float DEFAULT_SCALE = 1f;
    private static final float OFFSETX = 0.0f;
    private static final float OFFSETY = 0.0f;
    private static final float DEFAULT_ALPHA = 1f;
    private static final float DEFAULT_FPS = 0.5f;
    private static final float SCALEY = 0.9f;

    static protected Map<Pair<Float, Color>, BufferedImage[][][]> cachedImages = new HashMap<>(); // [mode][direction][frame]

    public enum Mode implements CharacterMode {
        WALK("walk", 15, true, DEFAULT_FPS),
        DEATH("death", 16, false, DEFAULT_FPS),
        IDLE("idle", 1, true, DEFAULT_FPS),
        TURN_LEFT("idle", 1, true, 0.6f),
        TURN_RIGHT("idle", 1, true, 0.6f),
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

    public Villager(UUID id, float scale, Color color) {
        super(id, Mode.values().length, OFFSETX, OFFSETY, false);
        init(new Pair<>(scale, color));
    }

    public Villager(UUID id) {
        this(id, DEFAULT_SCALE, null);
    }

    public Villager(float scale) {
        this(UUID.randomUUID(), scale, null);
    }

    public Villager(UUID id, float scale) {
        this(id, scale, null);
    }

    public Villager(float scale, Color color) {
        this(UUID.randomUUID(), scale, color);
    }

    public Villager() {
        this(UUID.randomUUID(), DEFAULT_SCALE, null);
    }

    public BufferedImage[][][] getCachedImages(Pair<Float, Color> pair) {
        return cachedImages.get(pair);
    }

    public void setCachedImages(Pair<Float, Color> pair, BufferedImage[][][] cachedImages) {
        Villager.cachedImages.put(pair, cachedImages);
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

            soundsInitialized = true; // pour éviter de recharger à chaque Villager
        }
    }

    protected void loadImages(Pair<Float, Color> pair) {
        Mode[] modes = Mode.values();
        for (Mode mode : modes) {
            switch (mode) {
                case DEATH:
                case IDLE:
                case WALK: {
                    int numFrames = mode.getNumFrames();
                    for (Direction direction : Direction.values()) {

                        BufferedImage[] frames = ImageLoader
                                .loadAnimations(
                                        numFrames,
                                        direction.ordinal(),
                                        String.format("/Villager_01/%s/villager_%s_",
                                                mode.getName(),
                                                mode.getName()),
                                        pair.first,
                                        SCALEY,
                                        pair.second);

                        images[mode.ordinal()][direction.ordinal()] = frames;

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

                    String name = String.format("/Villager_01/%s/villager_%s_",
                            mode.getName(),
                            mode.getName());

                    images[mode.ordinal()][0] = ImageLoader
                            .loadTurnAnimations(numFrames, 8, name, pair.first, SCALEY,
                                    null, left);

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
                case DEATH:
                case IDLE:
                case WALK: {
                    for (Direction direction : Direction.values()) {
                        animations[mode.ordinal()][direction.ordinal()] = new Animation(
                                images[mode.ordinal()][direction.ordinal()],
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
            case Mode.DEATH -> currentSound = Sound.DEATH;
            case Mode.TURN_LEFT -> currentDirection = Direction.values()[0];
            case Mode.TURN_RIGHT -> currentDirection = Direction.values()[0];
            case Mode.WALK_LEFT -> currentDirection = Direction.values()[0];
            case Mode.WALK_RIGHT -> currentDirection = Direction.values()[0];
            default -> {
            }
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
        TileMap.drawImage(_x, y, z, image, g, RenderType.CONSTRUCTION, DEFAULT_ALPHA, brightness);
        super.render(g, currentMode.ordinal());
    }

    public int getRank() {
        return 1;
    }

    @Override
    public boolean callTickTrigger(float frameIndex) {
        if (super.callTickTrigger(frameIndex)) {
            switch (currentMode) {
                case WALK -> {
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
        //setShadowPosition(x, y, z, offsetX, offsetY);
    }

    @Override
    public void setShadowPosition(float x, float y, float z) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setShadowPosition'");
    }

    @Override
    protected BufferedImage[][][] getCachedShadow_images(Pair<Float, Color> pair) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCachedShadow_images'");
    }

    @Override
    protected void setCachedShadow_images(Pair<Float, Color> pair, BufferedImage[][][] shadow_images) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCachedShadow_images'");
    }

}
