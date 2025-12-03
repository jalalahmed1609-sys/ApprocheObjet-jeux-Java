package fr.ubordeaux.ao.project07.engine;

import java.awt.Graphics;

public abstract class AbstractRenderable {

    public abstract RenderType getRenderType();

    public abstract void render(Graphics g);

    public abstract int getRank();

    public abstract float getIsoDepth();

    protected abstract float getX();

    protected abstract float getY();

    protected abstract float getZ();

}
