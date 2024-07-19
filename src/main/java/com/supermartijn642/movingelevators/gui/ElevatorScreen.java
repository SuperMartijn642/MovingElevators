package com.supermartijn642.movingelevators.gui;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.BlockEntityBaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import com.supermartijn642.movingelevators.blocks.ControllerBlockEntity;
import com.supermartijn642.movingelevators.packets.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Created 05/02/2022 by SuperMartijn642
 */
public class ElevatorScreen extends BlockEntityBaseWidget<ControllerBlockEntity> {

    public static final int MAX_NAME_LENGTH = 11;

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("movingelevators", "textures/gui/gui_background.png");
    private static final ResourceLocation SIZE_ICONS = ResourceLocation.fromNamespaceAndPath("movingelevators", "textures/gui/size_icons2.png");

    public ElevatorScreen(BlockPos entityPos){
        super(0, 0, 280, 118, ClientUtils.getWorld(), entityPos);
    }

    @Override
    protected Component getNarrationMessage(ControllerBlockEntity object){
        return null;
    }

    @Override
    protected void addWidgets(@NotNull ControllerBlockEntity blockEntity){
        // Floor name
        this.addWidget(new SynchingTextFieldWidget(6, 31, 84, MAX_NAME_LENGTH, () -> {
                String name = blockEntity.getFloorName();
                return name == null ? "" : name;
            }, name -> MovingElevators.CHANNEL.sendToServer(new PacketSetFloorName(this.blockEntityPos, name))))
            .setSuggestion(MovingElevatorsClient.formatFloorDisplayName(null, blockEntity.getGroup().getFloorNumber(blockEntity.getFloorLevel())));
        // Render buttons option
        this.addWidget(new CheckBoxWidget(42, 60,
            checked -> TextComponents.translation("movingelevators.elevator_screen.display_buttons", checked ? TextComponents.translation("movingelevators.elevator_screen.display_buttons.on").color(ChatFormatting.GREEN).get() : TextComponents.translation("movingelevators.elevator_screen.display_buttons.off").color(ChatFormatting.RED).get()).get(),
            () -> this.object.shouldShowButtons(),
            checked -> MovingElevators.CHANNEL.sendToServer(new PacketToggleShowControllerButtons(this.blockEntityPos))
        ));

        // Width
        PlusMinusButtonWidget widthSizeIncrease = this.addWidget(new PlusMinusButtonWidget(207, 31, true, TextComponents.translation("movingelevators.elevator_screen.cabin_width.increase_size").get(), () -> blockEntity.getGroup().canIncreaseCageWidth(), () -> MovingElevators.CHANNEL.sendToServer(new PacketIncreaseCabinWidth(this.blockEntityPos))));
        PlusMinusButtonWidget widthSizeDecrease = this.addWidget(new PlusMinusButtonWidget(230, 31, false, TextComponents.translation("movingelevators.elevator_screen.cabin_width.decrease_size").get(), () -> blockEntity.getGroup().canDecreaseCageWidth(), () -> MovingElevators.CHANNEL.sendToServer(new PacketDecreaseCabinWidth(this.blockEntityPos))));
        LeftRightArrowWidget widthOffsetDecrease = this.addWidget(new LeftRightArrowWidget(247, 31, true, TextComponents.translation("movingelevators.elevator_screen.cabin_width.decrease_offset").get(), () -> blockEntity.getGroup().canDecreaseCageSideOffset(), () -> MovingElevators.CHANNEL.sendToServer(new PacketDecreaseCabinSideOffset(this.blockEntityPos))));
        LeftRightArrowWidget widthOffsetIncrease = this.addWidget(new LeftRightArrowWidget(267, 31, false, TextComponents.translation("movingelevators.elevator_screen.cabin_width.increase_offset").get(), () -> blockEntity.getGroup().canIncreaseCageSideOffset(), () -> MovingElevators.CHANNEL.sendToServer(new PacketIncreaseCabinSideOffset(this.blockEntityPos))));
        // Depth
        PlusMinusButtonWidget depthSizeIncrease = this.addWidget(new PlusMinusButtonWidget(207, 47, true, TextComponents.translation("movingelevators.elevator_screen.cabin_depth.increase_size").get(), () -> blockEntity.getGroup().canIncreaseCageDepth(), () -> MovingElevators.CHANNEL.sendToServer(new PacketIncreaseCabinDepth(this.blockEntityPos))));
        PlusMinusButtonWidget depthSizeDecrease = this.addWidget(new PlusMinusButtonWidget(230, 47, false, TextComponents.translation("movingelevators.elevator_screen.cabin_depth.decrease_size").get(), () -> blockEntity.getGroup().canDecreaseCageDepth(), () -> MovingElevators.CHANNEL.sendToServer(new PacketDecreaseCabinDepth(this.blockEntityPos))));
        LeftRightArrowWidget depthOffsetDecrease = this.addWidget(new LeftRightArrowWidget(247, 47, true, TextComponents.translation("movingelevators.elevator_screen.cabin_depth.decrease_offset").get(), () -> blockEntity.getGroup().canDecreaseCageDepthOffset(), () -> MovingElevators.CHANNEL.sendToServer(new PacketDecreaseCabinDepthOffset(this.blockEntityPos))));
        LeftRightArrowWidget depthOffsetIncrease = this.addWidget(new LeftRightArrowWidget(267, 47, false, TextComponents.translation("movingelevators.elevator_screen.cabin_depth.increase_offset").get(), () -> blockEntity.getGroup().canIncreaseCageDepthOffset(), () -> MovingElevators.CHANNEL.sendToServer(new PacketIncreaseCabinDepthOffset(this.blockEntityPos))));
        // Height
        PlusMinusButtonWidget heightSizeIncrease = this.addWidget(new PlusMinusButtonWidget(207, 63, true, TextComponents.translation("movingelevators.elevator_screen.cabin_height.increase_size").get(), () -> blockEntity.getGroup().canIncreaseCageHeight(), () -> MovingElevators.CHANNEL.sendToServer(new PacketIncreaseCabinHeight(this.blockEntityPos))));
        PlusMinusButtonWidget heightSizeDecrease = this.addWidget(new PlusMinusButtonWidget(230, 63, false, TextComponents.translation("movingelevators.elevator_screen.cabin_height.decrease_size").get(), () -> blockEntity.getGroup().canDecreaseCageHeight(), () -> MovingElevators.CHANNEL.sendToServer(new PacketDecreaseCabinHeight(this.blockEntityPos))));
        LeftRightArrowWidget heightOffsetDecrease = this.addWidget(new LeftRightArrowWidget(247, 63, true, TextComponents.translation("movingelevators.elevator_screen.cabin_height.decrease_offset").get(), () -> blockEntity.getGroup().canDecreaseCageHeightOffset(), () -> MovingElevators.CHANNEL.sendToServer(new PacketDecreaseCabinHeightOffset(this.blockEntityPos))));
        LeftRightArrowWidget heightOffsetIncrease = this.addWidget(new LeftRightArrowWidget(267, 63, false, TextComponents.translation("movingelevators.elevator_screen.cabin_height.increase_offset").get(), () -> blockEntity.getGroup().canIncreaseCageHeightOffset(), () -> MovingElevators.CHANNEL.sendToServer(new PacketIncreaseCabinHeightOffset(this.blockEntityPos))));
        // Speed
        this.addWidget(new SliderWidget(190, 92, 84, 1, 10, (int)Math.round(blockEntity.getGroup().getTargetSpeed() * 10), speed -> TextComponents.translation("movingelevators.elevator_screen.current_speed", TextComponents.number(speed / 10d, 1).get()).get(), speed -> MovingElevators.CHANNEL.sendToServer(new PacketElevatorSpeed(this.blockEntityPos, speed / 10d))));

        // Cabin preview
        Supplier<BlockPos> previewSizeIncrease = () -> new BlockPos(widthSizeIncrease.active && widthSizeIncrease.isFocused() ? 1 : widthSizeDecrease.active && widthSizeDecrease.isFocused() ? -1 : 0, heightSizeIncrease.active && heightSizeIncrease.isFocused() ? 1 : heightSizeDecrease.active && heightSizeDecrease.isFocused() ? -1 : 0, depthSizeIncrease.active && depthSizeIncrease.isFocused() ? 1 : depthSizeDecrease.active && depthSizeDecrease.isFocused() ? -1 : 0);
        Supplier<BlockPos> previewOffset = () -> new BlockPos(widthOffsetIncrease.active && widthOffsetIncrease.isFocused() ? 1 : widthOffsetDecrease.active && widthOffsetDecrease.isFocused() ? -1 : 0, heightOffsetIncrease.active && heightOffsetIncrease.isFocused() ? 1 : heightOffsetDecrease.active && heightOffsetDecrease.isFocused() ? -1 : 0, depthOffsetIncrease.active && depthOffsetIncrease.isFocused() ? 1 : depthOffsetDecrease.active && depthOffsetDecrease.isFocused() ? -1 : 0);
        this.addWidget(new ElevatorPreviewWidget(99, 13, 82, 99, () -> this.object, previewSizeIncrease, previewOffset));
    }

    @Override
    protected void renderBackground(WidgetRenderContext context, int mouseX, int mouseY, ControllerBlockEntity object){
        // Background
        ScreenUtils.bindTexture(BACKGROUND);
        ScreenUtils.drawTexture(context.poseStack(), 0, 0, this.width(), this.height());

        super.renderBackground(context, mouseX, mouseY, object);
    }

    @Override
    protected void render(WidgetRenderContext context, int mouseX, int mouseY, ControllerBlockEntity blockEntity){
        // Size icons
        ScreenUtils.bindTexture(SIZE_ICONS);
        ScreenUtils.drawTexture(context.poseStack(), 190, 31, 11, 11, 0, 0, 1, 1 / 3f);
        ScreenUtils.drawTexture(context.poseStack(), 190, 47, 11, 11, 0, 1 / 3f, 1, 1 / 3f);
        ScreenUtils.drawTexture(context.poseStack(), 190, 63, 11, 11, 0, 2 / 3f, 1, 1 / 3f);

        // Size values
        ScreenUtils.drawCenteredString(context.poseStack(), TextComponents.number(blockEntity.getGroup().getCageWidth()).get(), 224, 34);
        ScreenUtils.drawCenteredString(context.poseStack(), TextComponents.number(blockEntity.getGroup().getCageSideOffset()).get(), 261, 34);
        ScreenUtils.drawCenteredString(context.poseStack(), TextComponents.number(blockEntity.getGroup().getCageDepth()).get(), 224, 50);
        ScreenUtils.drawCenteredString(context.poseStack(), TextComponents.number(blockEntity.getGroup().getCageDepthOffset()).get(), 261, 50);
        ScreenUtils.drawCenteredString(context.poseStack(), TextComponents.number(blockEntity.getGroup().getCageHeight()).get(), 224, 66);
        ScreenUtils.drawCenteredString(context.poseStack(), TextComponents.number(blockEntity.getGroup().getCageHeightOffset()).get(), 261, 66);

        // Text
        ScreenUtils.drawCenteredString(context.poseStack(), TextComponents.translation("movingelevators.elevator_screen.current_floor").get(), 47, 3, ScreenUtils.ACTIVE_TEXT_COLOR);
        ScreenUtils.drawCenteredString(context.poseStack(), TextComponents.translation("movingelevators.elevator_screen.elevator").get(), 232, 3, ScreenUtils.ACTIVE_TEXT_COLOR);
        ScreenUtils.drawString(context.poseStack(), TextComponents.translation("movingelevators.elevator_screen.floor_name").get(), 6, 18);
        ScreenUtils.drawString(context.poseStack(), TextComponents.translation("movingelevators.elevator_screen.show_buttons").get(), 6, 47);
        ScreenUtils.drawString(context.poseStack(), TextComponents.translation("movingelevators.elevator_screen.cabin_size").get(), 190, 18);
        ScreenUtils.drawString(context.poseStack(), TextComponents.translation("movingelevators.elevator_screen.elevator_speed").get(), 190, 79);

        super.render(context, mouseX, mouseY, blockEntity);
    }

    @Override
    protected void renderTooltips(WidgetRenderContext context, int mouseX, int mouseY, @NotNull ControllerBlockEntity blockEntity){
        if(mouseX >= 190 && mouseX <= 190 + 11 && mouseY >= 31 && mouseY <= 31 + 11)
            ScreenUtils.drawTooltip(context.poseStack(), TextComponents.translation("movingelevators.elevator_screen.cabin_width").get(), mouseX, mouseY);
        else if(mouseX >= 190 && mouseX <= 190 + 11 && mouseY >= 47 && mouseY <= 47 + 11)
            ScreenUtils.drawTooltip(context.poseStack(), TextComponents.translation("movingelevators.elevator_screen.cabin_depth").get(), mouseX, mouseY);
        else if(mouseX >= 190 && mouseX <= 190 + 11 && mouseY >= 63 && mouseY <= 63 + 11)
            ScreenUtils.drawTooltip(context.poseStack(), TextComponents.translation("movingelevators.elevator_screen.cabin_height").get(), mouseX, mouseY);

        super.renderTooltips(context, mouseX, mouseY, blockEntity);
    }
}
