package tconstruct.tools.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.util.Point;

import tconstruct.common.client.gui.GuiElement;
import tconstruct.common.client.gui.GuiElementScalable;
import tconstruct.common.client.gui.GuiModule;
import tconstruct.common.inventory.ContainerMultiModule;
import tconstruct.library.Util;
import tconstruct.library.client.CustomTextureCreator;
import tconstruct.library.client.ToolBuildGuiInfo;
import tconstruct.library.mantle.RecipeMatch;
import tconstruct.library.tinkering.MaterialItem;
import tconstruct.library.tinkering.PartMaterialType;
import tconstruct.library.tinkering.TinkersItem;
import tconstruct.library.tools.IToolPart;
import tconstruct.tools.client.module.GuiButtonsToolStation;
import tconstruct.tools.client.module.GuiInfoPanel;
import tconstruct.tools.client.module.GuiSideButtons;
import tconstruct.tools.tileentity.TileToolStation;

@SideOnly(Side.CLIENT)
public class GuiToolStation extends GuiTinkerStation {

  private static final ResourceLocation BACKGROUND = Util.getResource("textures/gui/toolstation.png");

  private static final GuiElement ItemCover = new GuiElement(176, 18, 80, 64, 256, 256);
  private static final GuiElement SlotBackground = new GuiElement(176, 0, 18, 18);
  private static final GuiElement SlotBorder = new GuiElement(194, 0, 18, 18);

  private static final GuiElement SlotSpaceTop = new GuiElement(0, 174+2, 18, 2);
  private static final GuiElement SlotSpaceBottom = new GuiElement(0, 174, 18, 2);
  private static final GuiElement PanelSpaceL = new GuiElement(0, 174, 5, 4);
  private static final GuiElement PanelSpaceR = new GuiElement(9, 174, 9, 4);

  private static final GuiElement BeamLeft = new GuiElement(0, 180, 2, 7);
  private static final GuiElement BeamRight = new GuiElement(131, 180, 2, 7);
  private static final GuiElementScalable BeamCenter = new GuiElementScalable(2, 180, 129, 7);

  public static final int Column_Count = 5;
  private static final int Table_slot_count = 6;

  protected GuiElement buttonDecorationTop = SlotSpaceTop;
  protected GuiElement buttonDecorationBot = SlotSpaceBottom;
  protected GuiElement panelDecorationL = PanelSpaceL;
  protected GuiElement panelDecorationR = PanelSpaceR;

  protected GuiElement beamL = new GuiElement(0, 0, 0, 0);
  protected GuiElement beamR = new GuiElement(0, 0, 0, 0);
  protected GuiElementScalable beamC = new GuiElementScalable(0, 0, 0, 0);

  protected GuiButtonsToolStation buttons;
  protected int activeSlots; // how many of the available slots are active

  protected GuiInfoPanel toolInfo;
  protected GuiInfoPanel traitInfo;

  protected ToolBuildGuiInfo currentInfo;


  public GuiToolStation(InventoryPlayer playerInv, World world, BlockPos pos, TileToolStation tile) {
    super(world, pos, (ContainerMultiModule) tile.createContainer(playerInv, world, pos));

    buttons = new GuiButtonsToolStation(this, inventorySlots);
    this.addModule(buttons);
    toolInfo = new GuiInfoPanel(this, inventorySlots);
    this.addModule(toolInfo);
    traitInfo = new GuiInfoPanel(this, inventorySlots);
    this.addModule(traitInfo);

    toolInfo.yOffset = 5;
    traitInfo.yOffset = toolInfo.getYSize() + 9;

    this.ySize = 174;

    wood();
  }

  @Override
  public void initGui() {
    super.initGui();

    // workaround to line up the tabs on switching even though the GUI is a tad higher
    this.guiTop += 4;
    this.cornerY += 4;

    buttons.xOffset = -2;
    buttons.yOffset = beamC.h + buttonDecorationTop.h;
    toolInfo.xOffset = 2;
    toolInfo.yOffset = beamC.h + panelDecorationL.h;
    traitInfo.xOffset = toolInfo.xOffset;
    traitInfo.yOffset = toolInfo.yOffset + toolInfo.getYSize() + 4;

    for(GuiModule module : modules) {
      module.guiTop += 4;
    }
  }

  public void onToolSelection(ToolBuildGuiInfo info) {
    activeSlots = Math.min(info.positions.size(), Table_slot_count);
    currentInfo = info;

    int i;
    for(i = 0; i < activeSlots; i++) {
      Point point = info.positions.get(i);

      Slot slot = inventorySlots.getSlot(i);
      slot.xDisplayPosition = point.getX();
      slot.yDisplayPosition = point.getY();
    }

    // remaining slots
    int stillFilled = 0;
    for(; i < Table_slot_count; i++) {
      Slot slot = inventorySlots.getSlot(i);
      if(slot.getHasStack()) {
        slot.xDisplayPosition = 87 + 20 * stillFilled;
        slot.yDisplayPosition = 62;
        stillFilled++;
      }
      else {
        // todo: slot.disable
        slot.xDisplayPosition = 0;
        slot.yDisplayPosition = 0;
      }
    }

    toolInfo.setText(new String[]{"Tool name", "Desc1", "Desc2", "Desc3", null, "Desc4", "Desc5"});
    traitInfo.setText(new String[]{"Traits", "Awesome",
                                   "This is a long desc with lorem ipsum blabla bla bla bla bla bla blabla lba bal bal balb al abl abla blablablablabal bla bla balbal bal ba laballbalbalbalalalb laballab mrgrhlomlbl amlm",
                                   "foobar"});
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    drawBackground(BACKGROUND);

    int xOff = 0;
    int yOff = 0;

    int x = 0;
    int y = 0;

    // draw the item background
    final float scale = 4.0f;
    GlStateManager.scale(scale, scale, 1.0f);
    //renderItemIntoGuiBackground(back, (this.cornerX + 15) / 4 + xOff, (this.cornerY + 18) / 4 + yOff);
    {
      int logoX = (this.cornerX + 10) / 4 + xOff;
      int logoY = (this.cornerY + 18) / 4 + yOff;

      if(currentInfo != null) {
        if(currentInfo.tool != null) {
          itemRender.renderItemIntoGUI(currentInfo.tool, logoX, logoY);
        }
        else if(currentInfo == GuiButtonRepair.info) {
          this.mc.getTextureManager().bindTexture(ICONS);
          ICON_Anvil.draw(logoX, logoY);
        }
      }
    }
    GlStateManager.scale(1f / scale, 1f / scale, 1.0f);

    // rebind gui texture
    this.mc.getTextureManager().bindTexture(BACKGROUND);

    // reset state after item drawing
    GlStateManager.enableBlend();
    GlStateManager.enableAlpha();
    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableDepth();

    // draw the halftransparent "cover" over the item
    GlStateManager.color(1.0f, 1.0f, 1.0f, 0.82f);
    ItemCover.draw(this.cornerX + 7, this.cornerY + 18);

    // the slot backgrounds
    GlStateManager.color(1.0f, 1.0f, 1.0f, 0.28f);
    for(int i = 0; i < activeSlots; i++) {
      Slot slot = inventorySlots.getSlot(i);
      SlotBackground.draw(x + this.cornerX + slot.xDisplayPosition - 1, y + this.cornerY + slot.yDisplayPosition - 1);
    }

    // full opaque. Draw the borders of the slots
    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    for(int i = 0; i < activeSlots; i++) {
      Slot slot = inventorySlots.getSlot(i);
      SlotBorder.draw(
          x + this.cornerX + slot.xDisplayPosition - 1, y + this.cornerY + slot.yDisplayPosition - 1);
    }

    this.mc.getTextureManager().bindTexture(ICONS);

    // slot logos
    if(currentInfo == GuiButtonRepair.info) {
      drawRepairSlotIcons();
    }
    else if(currentInfo.tool != null && currentInfo.tool.getItem() instanceof TinkersItem) {
      PartMaterialType[] pmts = ((TinkersItem) currentInfo.tool.getItem()).requiredComponents;
      for(int i = 0; i < activeSlots; i++) {
        if(i >= pmts.length) {
          continue;
        }

        IToolPart part = pmts[i].getPossibleParts().iterator().next();
        if(!(part instanceof MaterialItem)) {
          continue;
        }

        ItemStack stack = ((MaterialItem) part).getItemstackWithMaterial(CustomTextureCreator.guiMaterial);
        Slot slot = inventorySlots.getSlot(i);
        itemRender.renderItemIntoGUI(stack, x + this.cornerX + slot.xDisplayPosition, y + this.cornerY + slot.yDisplayPosition);
      }
    }


    this.mc.getTextureManager().bindTexture(BACKGROUND);
    x = buttons.guiLeft - beamL.w;
    y = cornerY;
    // draw the beams at the top
    x += beamL.draw(x, y);
    x += beamC.drawScaledX(x, y, buttons.xSize);
    beamR.draw(x, y);

    x = toolInfo.guiLeft - beamL.w;
    x += beamL.draw(x, y);
    x += beamC.drawScaledX(x, y, toolInfo.xSize);
    beamR.draw(x, y);

    // draw the decoration for the buttons
    for(Object o : buttons.buttonList) {
      GuiButton button = (GuiButton) o;

      buttonDecorationTop.draw(button.xPosition, button.yPosition - buttonDecorationTop.h);
      // don't draw the bottom for the buttons in the last row
      if(button.id < buttons.buttonList.size() - Column_Count) {
        buttonDecorationBot.draw(button.xPosition, button.yPosition + button.height);
      }
    }

    // draw the decorations for the panels
    panelDecorationL.draw(toolInfo.guiLeft + 5, toolInfo.guiTop - panelDecorationL.h);
    panelDecorationR.draw(toolInfo.guiRight() - 5 - panelDecorationR.w, toolInfo.guiTop - panelDecorationR.h);
    panelDecorationL.draw(traitInfo.guiLeft + 5, traitInfo.guiTop - panelDecorationL.h);
    panelDecorationR.draw(traitInfo.guiRight() - 5 - panelDecorationR.w, traitInfo.guiTop - panelDecorationR.h);

    GlStateManager.enableDepth();

    // continue as usual and hope that the drawing state is not completely wrecked
    super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
  }

  protected void drawRepairSlotIcons() {
    for(int i = 0; i < activeSlots; i++) {
      drawRepairSlotIcon(i);
    }
  }

  protected void drawRepairSlotIcon(int i) {
    GuiElement icon = null;
    Slot slot = inventorySlots.getSlot(i);

    if(i == 0) {
      icon = ICON_Pickaxe;
    }
    else if(i == 1) {
      icon = ICON_Dust;
    }
    else if(i == 2) {
      icon = ICON_Lapis;
    }
    else if(i == 3) {
      icon = ICON_Ingot;
    }
    else if(i == 4) {
      icon = ICON_Gem;
    }
    else if(i == 5) {
      icon = ICON_Quartz;
    }

    if(icon != null) {
      drawIcon(slot, icon);
    }
  }

  protected void wood() {
    toolInfo.wood();
    traitInfo.wood();

    buttonDecorationTop = SlotSpaceTop.shift(SlotSpaceTop.w,0);
    buttonDecorationBot = SlotSpaceBottom.shift(SlotSpaceBottom.w,0);
    panelDecorationL = PanelSpaceL.shift(18, 0);
    panelDecorationR = PanelSpaceR.shift(18, 0);

    buttons.wood();

    beamL = BeamLeft;
    beamR = BeamRight;
    beamC = BeamCenter;
  }

  protected void metal() {
    toolInfo.metal();
    traitInfo.metal();

    buttonDecorationTop = SlotSpaceTop.shift(SlotSpaceTop.w*2,0);
    buttonDecorationBot = SlotSpaceBottom.shift(SlotSpaceBottom.w*2,0);
    panelDecorationL = PanelSpaceL.shift(18*2, 0);
    panelDecorationR = PanelSpaceR.shift(18*2, 0);

    buttons.metal();

    beamL = BeamLeft.shift(0, BeamLeft.h);
    beamR = BeamRight.shift(0, BeamRight.h);
    beamC = BeamCenter.shift(0, BeamCenter.h);
  }
}
