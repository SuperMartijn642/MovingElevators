package com.supermartijn642.movingelevators.mixin;

import com.google.common.collect.Lists;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created 26/04/2023 by SuperMartijn642
 */
public class MovingElevatorsMixinPlugin implements IMixinConfigPlugin {

    private boolean isSodiumLoaded;
    private boolean isIrisLoaded;

    @Override
    public void onLoad(String mixinPackage){
        this.isSodiumLoaded = isClassAvailable("me.jellysquid.mods.sodium.client.SodiumClientMod");
        this.isIrisLoaded = isClassAvailable("net.coderbot.iris.Iris");
    }

    private static boolean isClassAvailable(String location){
        try{
            MixinService.getService().getBytecodeProvider().getClassNode(location);
            return true;
        }catch(Exception ignored){
            return false;
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
        List<String> mixins = new ArrayList<>();
        if(this.isSodiumLoaded)
            mixins.addAll(Lists.newArrayList("sodium.LevelRendererMixinSodium", "sodium.SodiumWorldRendererMixin"));
        if(this.isIrisLoaded)
            mixins.add("iris.ShadowRendererMixin");
        return mixins;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo){
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo){
    }
}
