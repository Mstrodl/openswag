package tech.coolmathgames.swag.driver;

import li.cil.oc.common.Tier;
import li.cil.oc.common.container.ComponentSlot;
import li.cil.oc.common.container.Player;
import li.cil.oc.common.container.StaticComponentSlot;
import li.cil.oc.server.agent.Inventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import tech.coolmathgames.swag.component.CardBoxDriver;

public class ContainerCardBox extends Player {
  private final CardBoxDriver chestInventory;

  public static final int xSize = 176;
  public static final int ySize = 133;

  public ContainerCardBox(InventoryPlayer inventoryPlayer, CardBoxDriver chestInventory) {
    super(inventoryPlayer, chestInventory);
    this.chestInventory = chestInventory;
    this.chestInventory.openInventory(inventoryPlayer.player);
    this.layoutContainer(inventoryPlayer, chestInventory);
  }

  @Override
  public boolean canInteractWith(EntityPlayer player) {
    return this.chestInventory.isUsableByPlayer(player);
  }

  protected void layoutContainer(InventoryPlayer playerInventory, IInventory chestInventory) {
    int leftCol = (xSize - 162) / 2 + 1;
    int center = ((xSize - (leftCol * 2)) / 2) - ((18 * chestInventory.getSizeInventory()) / 2);

    for(int i = 0; i < chestInventory.getSizeInventory(); ++i) {
      StaticComponentSlot slot = new StaticComponentSlot(this, this.chestInventory, i, leftCol + center + i * 18, 19, li.cil.oc.common.Slot.Card(), Tier.Three());
      this.addSlotToContainer(slot);
    }

    for (int playerInvRow = 0; playerInvRow < 3; playerInvRow++) {
      for (int playerInvCol = 0; playerInvCol < 9; playerInvCol++) {
        this.addSlotToContainer(
                new Slot(playerInventory, playerInvCol + playerInvRow * 9 + 9, leftCol + playerInvCol * 18, ySize - (4 - playerInvRow) * 18 - 10));
      }
    }

    for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
      this.addSlotToContainer(new Slot(playerInventory, hotbarSlot, leftCol + hotbarSlot * 18, ySize - 24));
    }
  }
}
