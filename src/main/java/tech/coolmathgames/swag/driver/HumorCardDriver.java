package tech.coolmathgames.swag.driver;

import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverItem;
import net.minecraft.item.ItemStack;
import tech.coolmathgames.swag.component.HumorCardComponent;
import tech.coolmathgames.swag.item.ItemHumorCard;

public class HumorCardDriver extends DriverItem {
  public static HumorCardDriver driver = new HumorCardDriver();

  public HumorCardDriver() {
    super(ItemHumorCard.DEFAULTSTACK);
  }

  @Override
  public ManagedEnvironment createEnvironment(ItemStack itemStack, EnvironmentHost environmentHost) {
    if(environmentHost.world() != null && environmentHost.world().isRemote) {
      return null;
    } else {
      return new HumorCardComponent(environmentHost);
    }
  }

  @Override
  public String slot(ItemStack stack) {
    return Slot.Card;
  }
}
