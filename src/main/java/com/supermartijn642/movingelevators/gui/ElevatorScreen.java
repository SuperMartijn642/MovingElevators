package com.supermartijn642.movingelevators.gui;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.TileEntityBaseScreen;
import com.supermartijn642.core.gui.widget.Widget;
import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import com.supermartijn642.movingelevators.blocks.ControllerBlockEntity;
import com.supermartijn642.movingelevators.packets.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Created 05/02/2022 by SuperMartijn642
 */
public class ElevatorScreen extends TileEntityBaseScreen<ControllerBlockEntity> {

    public static final int MAX_NAME_LENGTH = 11;

    private static final ResourceLocation BACKGROUND = new ResourceLocation("movingelevators", "textures/gui/gui_background.png");
    private static final ResourceLocation SIZE_ICONS = new ResourceLocation("movingelevators", "textures/gui/size_icons2.png");

    public ElevatorScreen(BlockPos tilePos){
        super(TextComponents.empty().get(), tilePos);
    }

    @Override
    protected float sizeX(@Nonnull ControllerBlockEntity blockEntity){
        return 280;
    }

    @Override
    protected float sizeY(@Nonnull ControllerBlockEntity blockEntity){
        return 118;
    }

    @Override
    protected void addWidgets(@Nonnull ControllerBlockEntity blockEntity){
        // Floor name
        this.addWidget(new SynchingTextFieldWidget(6, 31, 84, MAX_NAME_LENGTH, () -> {
                String name = blockEntity.getFloorName();
                return name == null ? "" : name;
            }, name -> MovingElevators.CHANNEL.sendToServer(new PacketSetFloorName(this.tilePos, name))))
            .setSuggestion(MovingElevatorsClient.formatFloorDisplayName(null, blockEntity.getGroup().getFloorNumber(blockEntity.getFloorLevel())));
        // Render buttons option
        this.addWidget(new CheckBoxWidget(42, 60,
            checked -> TextComponents.translation("movingelevators.elevator_screen.display_buttons", checked ? TextComponents.translation("movingelevators.elevator_screen.display_buttons.on").color(TextFormatting.GREEN).get() : TextComponents.translation("movingelevators.elevator_screen.display_buttons.off").color(TextFormatting.RED).get()).get(),
            () -> this.getObjectOrClose().shouldShowButtons(),
            checked -> MovingElevators.CHANNEL.sendToServer(new PacketToggleShowControllerButtons(this.tilePos))
        ));

        // Width
        Widget widthSizeIncrease = this.addWidget(new PlusMinusButtonWidget(207, 31, true, TextComponents.translation("movingelevators.elevator_screen.cabin_width.increase_size").get(), () -> blockEntity.getGroup().canIncreaseCageWidth(), () -> MovingElevators.CHANNEL.sendToServer(new PacketIncreaseCabinWidth(this.tilePos))));
        Widget widthSizeDecrease = this.addWidget(new PlusMinusButtonWidget(230, 31, false, TextComponents.translation("movingelevators.elevator_screen.cabin_width.decrease_size").get(), () -> blockEntity.getGroup().canDecreaseCageWidth(), () -> MovingElevators.CHANNEL.sendToServer(new PacketDecreaseCabinWidth(this.tilePos))));
        Widget widthOffsetDecrease = this.addWidget(new LeftRightArrowWidget(247, 31, true, TextComponents.translation("movingelevators.elevator_screen.cabin_width.decrease_offset").get(), () -> blockEntity.getGroup().canDecreaseCageSideOffset(), () -> MovingElevators.CHANNEL.sendToServer(new PacketDecreaseCabinSideOffset(this.tilePos))));
        Widget widthOffsetIncrease = this.addWidget(new LeftRightArrowWidget(267, 31, false, TextComponents.translation("movingelevators.elevator_screen.cabin_width.increase_offset").get(), () -> blockEntity.getGroup().canIncreaseCageSideOffset(), () -> MovingElevators.CHANNEL.sendToServer(new PacketIncreaseCabinSideOffset(this.tilePos))));
        // Depth
        Widget depthSizeIncrease = this.addWidget(new PlusMinusButtonWidget(207, 47, true, TextComponents.translation("movingelevators.elevator_screen.cabin_depth.increase_size").get(), () -> blockEntity.getGroup().canIncreaseCageDepth(), () -> MovingElevators.CHANNEL.sendToServer(new PacketIncreaseCabinDepth(this.tilePos))));
        Widget depthSizeDecrease = this.addWidget(new PlusMinusButtonWidget(230, 47, false, TextComponents.translation("movingelevators.elevator_screen.cabin_depth.decrease_size").get(), () -> blockEntity.getGroup().canDecreaseCageDepth(), () -> MovingElevators.CHANNEL.sendToServer(new PacketDecreaseCabinDepth(this.tilePos))));
        Widget depthOffsetDecrease = this.addWidget(new LeftRightArrowWidget(247, 47, true, TextComponents.translation("movingelevators.elevator_screen.cabin_depth.decrease_offset").get(), () -> blockEntity.getGroup().canDecreaseCageDepthOffset(), () -> MovingElevators.CHANNEL.sendToServer(new PacketDecreaseCabinDepthOffset(this.tilePos))));
        Widget depthOffsetIncrease = this.addWidget(new LeftRightArrowWidget(267, 47, false, TextComponents.translation("movingelevators.elevator_screen.cabin_depth.increase_offset").get(), () -> blockEntity.getGroup().canIncreaseCageDepthOffset(), () -> MovingElevators.CHANNEL.sendToServer(new PacketIncreaseCabinDepthOffset(this.tilePos))));
        // Height
        Widget heightSizeIncrease = this.addWidget(new PlusMinusButtonWidget(207, 63, true, TextComponents.translation("movingelevators.elevator_screen.cabin_height.increase_size").get(), () -> blockEntity.getGroup().canIncreaseCageHeight(), () -> MovingElevators.CHANNEL.sendToServer(new PacketIncreaseCabinHeight(this.tilePos))));
        Widget heightSizeDecrease = this.addWidget(new PlusMinusButtonWidget(230, 63, false, TextComponents.translation("movingelevators.elevator_screen.cabin_height.decrease_size").get(), () -> blockEntity.getGroup().canDecreaseCageHeight(), () -> MovingElevators.CHANNEL.sendToServer(new PacketDecreaseCabinHeight(this.tilePos))));
        Widget heightOffsetDecrease = this.addWidget(new LeftRightArrowWidget(247, 63, true, TextComponents.translation("movingelevators.elevator_screen.cabin_height.decrease_offset").get(), () -> blockEntity.getGroup().canDecreaseCageHeightOffset(), () -> MovingElevators.CHANNEL.sendToServer(new PacketDecreaseCabinHeightOffset(this.tilePos))));
        Widget heightOffsetIncrease = this.addWidget(new LeftRightArrowWidget(267, 63, false, TextComponents.translation("movingelevators.elevator_screen.cabin_height.increase_offset").get(), () -> blockEntity.getGroup().canIncreaseCageHeightOffset(), () -> MovingElevators.CHANNEL.sendToServer(new PacketIncreaseCabinHeightOffset(this.tilePos))));
        // Speed
        this.addWidget(new SliderWidget(190, 92, 84, 1, 10, (int)Math.round(blockEntity.getGroup().getTargetSpeed() * 10), speed -> TextComponents.translation("movingelevators.elevator_screen.current_speed", TextComponents.number(speed / 10d, 1).get()).get(), speed -> MovingElevators.CHANNEL.sendToServer(new PacketElevatorSpeed(this.tilePos, speed / 10d))));

        // Cabin preview
        Supplier<BlockPos> previewSizeIncrease = () -> new BlockPos(widthSizeIncrease.active && widthSizeIncrease.isHovered() ? 1 : widthSizeDecrease.active && widthSizeDecrease.isHovered() ? -1 : 0, heightSizeIncrease.active && heightSizeIncrease.isHovered() ? 1 : heightSizeDecrease.active && heightSizeDecrease.isHovered() ? -1 : 0, depthSizeIncrease.active && depthSizeIncrease.isHovered() ? 1 : depthSizeDecrease.active && depthSizeDecrease.isHovered() ? -1 : 0);
        Supplier<BlockPos> previewOffset = () -> new BlockPos(widthOffsetIncrease.active && widthOffsetIncrease.isHovered() ? 1 : widthOffsetDecrease.active && widthOffsetDecrease.isHovered() ? -1 : 0, heightOffsetIncrease.active && heightOffsetIncrease.isHovered() ? 1 : heightOffsetDecrease.active && heightOffsetDecrease.isHovered() ? -1 : 0, depthOffsetIncrease.active && depthOffsetIncrease.isHovered() ? 1 : depthOffsetDecrease.active && depthOffsetDecrease.isHovered() ? -1 : 0);
        this.addWidget(new ElevatorPreviewWidget(99, 13, 82, 99, this::getObjectOrClose, previewSizeIncrease, previewOffset));
    }

    @Override
    protected void render(int mouseX, int mouseY, @Nonnull ControllerBlockEntity blockEntity){
        // Background
        ScreenUtils.bindTexture(BACKGROUND);
        ScreenUtils.drawTexture(0, 0, this.sizeX(blockEntity), this.sizeY(blockEntity));

        // Size icons
        ScreenUtils.bindTexture(SIZE_ICONS);
        ScreenUtils.drawTexture(190, 31, 11, 11, 0, 0, 1, 1 / 3f);
        ScreenUtils.drawTexture(190, 47, 11, 11, 0, 1 / 3f, 1, 1 / 3f);
        ScreenUtils.drawTexture(190, 63, 11, 11, 0, 2 / 3f, 1, 1 / 3f);

        // Size values
        ScreenUtils.drawCenteredString(TextComponents.number(blockEntity.getGroup().getCageWidth()).get(), 224, 34);
        ScreenUtils.drawCenteredString(TextComponents.number(blockEntity.getGroup().getCageSideOffset()).get(), 261, 34);
        ScreenUtils.drawCenteredString(TextComponents.number(blockEntity.getGroup().getCageDepth()).get(), 224, 50);
        ScreenUtils.drawCenteredString(TextComponents.number(blockEntity.getGroup().getCageDepthOffset()).get(), 261, 50);
        ScreenUtils.drawCenteredString(TextComponents.number(blockEntity.getGroup().getCageHeight()).get(), 224, 66);
        ScreenUtils.drawCenteredString(TextComponents.number(blockEntity.getGroup().getCageHeightOffset()).get(), 261, 66);

        // Text
        ScreenUtils.drawCenteredString(TextComponents.translation("movingelevators.elevator_screen.current_floor").get(), 47, 3, ScreenUtils.ACTIVE_TEXT_COLOR);
        ScreenUtils.drawCenteredString(TextComponents.translation("movingelevators.elevator_screen.elevator").get(), 232, 3, ScreenUtils.ACTIVE_TEXT_COLOR);
        ScreenUtils.drawString(TextComponents.translation("movingelevators.elevator_screen.floor_name").get(), 6, 18);
        ScreenUtils.drawString(TextComponents.translation("movingelevators.elevator_screen.show_buttons").get(), 6, 47);
        ScreenUtils.drawString(TextComponents.translation("movingelevators.elevator_screen.cabin_size").get(), 190, 18);
        ScreenUtils.drawString(TextComponents.translation("movingelevators.elevator_screen.elevator_speed").get(), 190, 79);
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY, @Nonnull ControllerBlockEntity blockEntity){
        if(mouseX >= 190 && mouseX <= 190 + 11 && mouseY >= 31 && mouseY <= 31 + 11)
            this.drawHoveringText(TextComponents.translation("movingelevators.elevator_screen.cabin_width").format(), mouseX, mouseY);
        else if(mouseX >= 190 && mouseX <= 190 + 11 && mouseY >= 47 && mouseY <= 47 + 11)
            this.drawHoveringText(TextComponents.translation("movingelevators.elevator_screen.cabin_depth").format(), mouseX, mouseY);
        else if(mouseX >= 190 && mouseX <= 190 + 11 && mouseY >= 63 && mouseY <= 63 + 11)
            this.drawHoveringText(TextComponents.translation("movingelevators.elevator_screen.cabin_height").format(), mouseX, mouseY);
    }
}
