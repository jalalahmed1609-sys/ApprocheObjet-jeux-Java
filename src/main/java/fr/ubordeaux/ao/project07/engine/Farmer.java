package fr.ubordeaux.ao.project07.engine;
import fr.ubordeaux.ao.project07.Crusader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Farmer extends AbstractCharacter<CharacterMode> {

    private static final float DEFAULT_SCALE = 1f;
    private static final float OFFSETX = 0.0f;
    private static final float OFFSETY = 0.0f;
    private static final float DEFAULT_ALPHA = 1f;
    private static final float DEFAULT_FPS = 0.7f;
    private static final float SCALEY = 0.55f;
    
    static protected Map<Pair<Float, Color>, BufferedImage[][][]> cachedImages = new HashMap<>(); // [mode][direction][frame]

    public enum Mode implements CharacterMode {
        HOE("Hoe", 13, true, DEFAULT_FPS),
        IDLE("Idle", 4, true, DEFAULT_FPS),
        PLANT("Plant", 11, true, DEFAULT_FPS),
        WALK("Walk", 15, true, DEFAULT_FPS),
        WCAN("wCan", 18, true, DEFAULT_FPS);

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

    public Farmer(UUID id, float scale, Color color) {
        super(id, Mode.values().length, OFFSETX, OFFSETY, false);
        init(new Pair<>(scale, color));
    }

    public Farmer(UUID id) {
        this(id, DEFAULT_SCALE, null);
    }

    public Farmer(float scale) {
        this(UUID.randomUUID(), scale, null);
    }

    public Farmer(UUID id, float scale) {
        this(id, scale, null);
    }

    public Farmer(float scale, Color color) {
        this(UUID.randomUUID(), scale, color);
    }

    public Farmer() {
        this(UUID.randomUUID(), DEFAULT_SCALE, null);
    }

    public BufferedImage[][][] getCachedImages(Pair<Float, Color> pair) {
        return cachedImages.get(pair);
    }

    public void setCachedImages(Pair<Float, Color> pair, BufferedImage[][][] cachedImages) {
        Farmer.cachedImages.put(pair, cachedImages);
    }

    private void init(Pair<Float, Color> pair) {
        savedMode = currentMode = Mode.WALK;
        currentSound = Sound.CARPET_SOFT;
        currentDirection = Direction.EAST;
        initSounds();
        initAnimations(pair);
    }

    private void initSounds() {
        if (!soundsInitialized) {
            SoundCache.loadIfAbsent("CARPET", "/kenney_impact-sounds/Audio/footstep_carpet_000.wav", 0.8f);
            //SoundCache.loadIfAbsent("CARPET_SOFT", "/kenney_impact-sounds/Audio/footstep_carpet_000.wav", 0.2f);
            //SoundCache.loadIfAbsent("WOOD", "/kenney_impact-sounds/Audio/footstep_wood_000.wav", 0.8f);
            //SoundCache.loadIfAbsent("WOOD_SOFT", "/kenney_impact-sounds/Audio/footstep_wood_000.wav", 0.2f);
            //SoundCache.loadIfAbsent("JUMP", "/kenney_impact-sounds/Audio/footstep_carpet_000.wav", 0.8f);
            //SoundCache.loadIfAbsent("GOTHIT", "/kenney_impact-sounds/Audio/impactTin_medium_000.wav", 0.8f);
            //SoundCache.loadIfAbsent("ATTACK", "/kenney_impact-sounds/Audio/impactPlate_heavy_000.wav", 0.8f);
            //SoundCache.loadIfAbsent("DEATH", "/kenney_impact-sounds/Audio/impactWood_heavy_000.wav", 0.8f);

            soundsInitialized = true; // pour éviter de recharger à chaque Villager
        }
    }

    protected void loadImages(Pair<Float, Color> pair) {
        Mode[] modes = Mode.values();
        for (Mode mode : modes) {
            switch (mode) {
                case HOE:
                case IDLE:
                case PLANT:
                case WALK: 
                case WCAN:
                {
                    int numFrames = mode.getNumFrames();

                    

                    for (Direction direction : Direction.values()) {
                        int correctedDirection = (direction.ordinal() + 6) % 8;

                        String path = String.format("/Farmer/%s/%d_Farmer_%s_strip%s.png", mode.getName(), correctedDirection, 
                        mode.getName(), mode.getNumFrames());
                        
float scaleY = switch (mode) {
    case HOE -> SCALEY * (1.5f + 0.05f * (float) Math.sin(direction.getRadAngle()));
    case PLANT, WCAN -> SCALEY * (1 + 0.05f * (float) Math.sin(direction.getRadAngle()));
    case IDLE, WALK -> SCALEY;
};
                        BufferedImage[] frames = ImageLoader.loadCropAnimations(numFrames, path, pair.first, scaleY, pair.second);

                        images[mode.ordinal()][direction.ordinal()] = frames;

                    }
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
                case HOE: 
                case IDLE: 
                case PLANT: 
                case WALK: 
                case WCAN: 
                {
                    for (Direction direction : Direction.values()) {
                        animations[mode.ordinal()][direction.ordinal()] = new Animation(
                                images[mode.ordinal()][direction.ordinal()],
                                mode.numFrames,
                                mode.isLoop(), mode.getFps(), this);
                    }
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

    @Override
    public void setShadowPosition(float x, float y, float z) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setShadowPosition'");
    }

}
