package fr.ubordeaux.ao.project07.engine;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ImageSceneRenderable extends AbstractRenderable {

    private final int code;
    private final int x;
    private final int y;
    private final int z;
    private final BufferedImage bufferedImage;
    private final RenderType renderType;
    private final int rank;
    private final float alpha;
    private float iso;

    public ImageSceneRenderable(int code, int x, int y, int z, BufferedImage bufferedImage, 
            int rank,
            RenderType renderType, float iso, float alpha) {
        this.code = code;
        this.x = x;
        this.y = y;
        this.z = z;
        this.bufferedImage = bufferedImage;
        this.rank = rank;
        this.renderType = renderType;
        this.iso = iso;
        this.alpha = alpha;
    }

    public ImageSceneRenderable(BufferedImage bufferedImage, RenderType renderType, float iso) {
		this(0, 0, 0, 0, bufferedImage, 0, renderType, iso, 0f);
	}

    public ImageSceneRenderable(int code, int x, int y, int z) {
		this(code, x, y, z, null, 0, null, 0f, 0f);
	}

    public RenderType getRenderType() {
        return renderType;
    }

    public void render(Graphics g) {
        TileMap.drawImage(x, y, z, bufferedImage, g, renderType, alpha, 0f);
    }

    public int getRank() {
        return rank;
    }

    public float getIsoDepth() {
        return x + y + iso;
    }

	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

    @Override
    protected float getX() {
        return x;
    }

    @Override
    protected float getY() {
        return y;
    }

    @Override
    protected float getZ() {
        return z;
    }

    public float getIso() {
        return iso;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + code;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ImageSceneRenderable other = (ImageSceneRenderable) obj;
        if (code != other.code)
            return false;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        if (z != other.z)
            return false;
        return true;
    }

}
