package com.supermartijn642.movingelevators.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.List;
import java.util.Set;

/**
 * Created 26/04/2023 by SuperMartijn642
 */
public class MovingElevatorsMixinPlugin implements IMixinConfigPlugin {

    private boolean isSodiumLoaded;

    @Override
    public void onLoad(String mixinPackage){
        try {
            MixinService.getService().getBytecodeProvider().getClassNode("me.jellysquid.mods.sodium.client.SodiumClientMod");
            this.isSodiumLoaded = true;
        } catch (Exception ignored){
            this.isSodiumLoaded = false;
        }
    }

    @Override
    public String getRefMapperConfig(){
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName){
        return !(this.isSodiumLoaded && mixinClassName.endsWith(".LevelRendererMixin"));
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets){
    }

    @Override
    public List<String> getMixins(){
        return this.isSodiumLoaded ?
            List.of("sodium.LevelRendererMixinSodium", "sodium.SodiumWorldRendererMixin")
            : null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo){
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo){
    }
}
