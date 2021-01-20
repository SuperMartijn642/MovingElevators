package com.supermartijn642.movingelevators.base;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

/**
 * Created 4/7/2020 by SuperMartijn642
 */
public abstract class METileRenderer<T extends METile> extends TileEntitySpecialRenderer<T> {

    protected T tile;
    protected double x, y, z;
    protected float partialTicks;
    protected int destroyStage;
    protected float alpha;

    public METileRenderer(){
        super();
    }

    @Override
    public void render(T tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha){
        if(tile == null || tile.getWorld() == null)
            return;

        this.tile = tile;
        this.x = x;
        this.y = y;
        this.z = z;
        this.partialTicks = partialTicks;
        this.destroyStage = destroyStage;
        this.alpha = alpha;

        this.render();
    }

    protected abstract void render();
}
