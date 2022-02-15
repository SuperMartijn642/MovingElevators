package com.supermartijn642.movingelevators.data;

import com.supermartijn642.movingelevators.MovingElevators;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

/**
 * Created 05/01/2022 by SuperMartijn642
 */
public class MovingElevatorsBlockTagsProvider extends BlockTagsProvider {

    public MovingElevatorsBlockTagsProvider(GatherDataEvent e){
        super(e.getGenerator(), "movingelevators", e.getExistingFileHelper());
    }

    @Override
    protected void addTags(){
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(MovingElevators.elevator_block, MovingElevators.display_block, MovingElevators.button_block);
    }
}
