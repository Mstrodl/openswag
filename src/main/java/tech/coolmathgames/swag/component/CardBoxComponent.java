package tech.coolmathgames.swag.component;

import li.cil.oc.api.API;
import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

public class CardBoxComponent extends BaseComponent {
  private final ItemStackHandler inventory = new ItemStackHandler(16);

  public CardBoxComponent(EnvironmentHost host) {
    super("swag_card_box", host);
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    nbt.setTag("inventory", inventory.serializeNBT());
    return nbt;
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt) {
    inventory.deserializeNBT(nbt.getCompoundTag("inventory"));

    for(int i = 0; i < 16; ++i) {
      ItemStack stack = this.inventory.getStackInSlot(i);
      if(stack != null) {
        DriverItem driver = Driver.driverFor(stack);
        ManagedEnvironment environment = driver.createEnvironment(stack, this.container);
        environment.load(driver.dataTag(stack));
        this.node.connect(environment.node());
      }
    }
  }
}
