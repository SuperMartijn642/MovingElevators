package com.supermartijn642.movingelevators.data;

import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

/**
 * Created 03/11/2021 by SuperMartijn642
 */
public class MEBlockTagsProvider extends BlockTagsProvider {

    public MEBlockTagsProvider(GatherDataEvent e){
        super(e.getGenerator(), "additionallanterns", e.getExistingFileHelper());
    }

    @Override
    protected void addTags(){
        TagsProvider.TagAppender<Block> pickaxeTag = this.tag(BlockTags.MINEABLE_WITH_PICKAXE);
        ForgeRegistries.BLOCKS.getEntries().stream()
            .filter(entry -> entry.getKey().location().getNamespace().equals("movingelevators"))
            .map(Map.Entry::getValue)
            .forEach(pickaxeTag::add);
    }
}
