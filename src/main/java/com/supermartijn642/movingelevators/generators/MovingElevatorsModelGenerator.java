package com.supermartijn642.movingelevators.generators;

import com.supermartijn642.core.generator.ModelGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * Created 12/09/2022 by SuperMartijn642
 */
public class MovingElevatorsModelGenerator extends ModelGenerator {

    public MovingElevatorsModelGenerator(ResourceCache cache){
        super("movingelevators", cache);
    }

    @Override
    public void generate(){
        this.cubeAll("block/elevator_block", ResourceLocation.fromNamespaceAndPath("movingelevators", "blocks/elevator"));
        this.cubeAll("block/display_block", ResourceLocation.fromNamespaceAndPath("movingelevators", "blocks/display"));
        this.cubeAll("block/button_block", ResourceLocation.fromNamespaceAndPath("movingelevators", "blocks/display"));
        this.model("item/elevator_block")
            .parent("block/elevator_block")
            .texture("overlay", "blocks/buttons")
            .element(element -> element.shape(0, 0, 0, 16, 16, 16).allFaces(face -> face.texture("all")))
            .element(element -> element.shape(0, 0, 0, 16, 16, 16).face(Direction.NORTH, face -> face.texture("overlay").uv(0, 0, 11.5f, 11.5f)));
        this.model("item/display_block")
            .parent("block/display_block")
            .texture("overlay", "blocks/display_overlay")
            .texture("buttons", "blocks/display_buttons")
            .element(element -> element.shape(0, 0, 0, 16, 16, 16).allFaces(face -> face.texture("all")))
            .element(element -> element.shape(0, 0, 0, 16, 16, 16).face(Direction.NORTH, face -> face.texture("overlay")))
            .element(element -> element.shape(0, 0, 0, 16, 16, 16).face(Direction.NORTH, face -> face.texture("buttons")));
        this.model("item/button_block")
            .parent("block/button_block")
            .texture("overlay", "blocks/buttons")
            .element(element -> element.shape(0, 0, 0, 16, 16, 16).allFaces(face -> face.texture("all")))
            .element(element -> element.shape(0, 0, 0, 16, 16, 16).face(Direction.NORTH, face -> face.texture("overlay").uv(0, 0, 11.5f, 11.5f)));
    }
}
