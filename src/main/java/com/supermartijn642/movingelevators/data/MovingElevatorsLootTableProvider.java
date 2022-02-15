package com.supermartijn642.movingelevators.data;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created 14/02/2022 by SuperMartijn642
 */
public class MovingElevatorsLootTableProvider extends LootTableProvider {

    public MovingElevatorsLootTableProvider(GatherDataEvent e){
        super(e.getGenerator());
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation,LootTable.Builder>>>,LootParameterSet>> getTables(){
        BlockLootTables lootTables = new BlockLootTables() {
            @Override
            protected Iterable<Block> getKnownBlocks(){
                return ForgeRegistries.BLOCKS.getValues().stream()
                    .filter(block -> block.getRegistryName().getNamespace().equals("movingelevators"))
                    .collect(Collectors.toList());
            }

            @Override
            protected void addTables(){
                ForgeRegistries.BLOCKS.getValues().stream()
                    .filter(block -> block.getRegistryName().getNamespace().equals("movingelevators"))
                    .forEach(this::dropSelf);
            }
        };

        return ImmutableList.of(Pair.of(() -> lootTables, LootParameterSets.BLOCK));
    }

    @Override
    protected void validate(Map<ResourceLocation,LootTable> map, ValidationTracker validationtracker){
        map.forEach((a, b) -> LootTableManager.validate(validationtracker, a, b));
    }
}
