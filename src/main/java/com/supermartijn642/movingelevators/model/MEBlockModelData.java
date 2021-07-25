package com.supermartijn642.movingelevators.model;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class MEBlockModelData implements IModelData {

    public final BlockState camouflage;

    public MEBlockModelData(BlockState camouflage){
        this.camouflage = camouflage;
    }

    @Override
    public boolean hasProperty(ModelProperty<?> prop){
        return false;
    }

    @Override
    public <T> T getData(ModelProperty<T> prop){
        return null;
    }

    @Override
    public <T> T setData(ModelProperty<T> prop, T data){
        return null;
    }
}
