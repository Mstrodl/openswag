package tech.coolmathgames.swag.driver;

import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverItem;
import net.minecraft.item.ItemStack;
import tech.coolmathgames.swag.OpenSwag;
import tech.coolmathgames.swag.component.DgramCardComponent;
import tech.coolmathgames.swag.item.ItemDgramCard;

public class DgramCardDriver extends DriverItem {
  public static DgramCardDriver driver = new DgramCardDriver();

  public DgramCardDriver() {
    super(ItemDgramCard.DEFAULTSTACK);
    OpenSwag.logger.info("Added Dgram Item to DgramCardDriver: " + ItemDgramCard.DEFAULTSTACK);
  }

  @Override
  public ManagedEnvironment createEnvironment(ItemStack itemStack, EnvironmentHost environmentHost) {
    if(environmentHost.world() != null && environmentHost.world().isRemote) {
      return null;
    } else {
      return new DgramCardComponent(environmentHost);
    }
  }

  @Override
  public String slot(ItemStack stack) {
    return Slot.Card;
  }
}
