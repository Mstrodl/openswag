package tech.coolmathgames.swag.driver;

import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverItem;
import net.minecraft.item.ItemStack;
import tech.coolmathgames.swag.component.ComputeCardComponent;
import tech.coolmathgames.swag.item.ItemComputeCard;

public class ComputeCardDriver extends DriverItem {
  public static ComputeCardDriver driver = new ComputeCardDriver();

  public ComputeCardDriver() {
    super(ItemComputeCard.DEFAULTSTACK);
  }

  @Override
  public ManagedEnvironment createEnvironment(ItemStack itemStack, EnvironmentHost environmentHost) {
    if(environmentHost.world() != null && environmentHost.world().isRemote) {
      return null;
    } else {
      return new ComputeCardComponent(environmentHost);
    }
  }

  @Override
  public String slot(ItemStack stack) {
    return Slot.Card;
  }
}
