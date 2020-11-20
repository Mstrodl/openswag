package tech.coolmathgames.swag;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import li.cil.oc.api.Driver;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tech.coolmathgames.swag.driver.DgramCardDriver;
import tech.coolmathgames.swag.driver.HumorCardDriver;
import tech.coolmathgames.swag.item.ItemDgramCard;
import tech.coolmathgames.swag.item.ItemHumorCard;

import javax.annotation.Nonnull;
import java.util.HashSet;

@Mod.EventBusSubscriber
public class ContentRegistry {
  public static CreativeTabs creativeTab = new CreativeTabs("tabOpenSwag") {
    public @Nonnull ItemStack getTabIconItem() {
      return ItemHumorCard.DEFAULTSTACK;
    }

    public @Nonnull String getTranslatedTabLabel() {
      return new TextComponentTranslation("itemGroup.OpenSwag.tabOpenSwag").getUnformattedText();
    }
  };

  public static final HashSet<ItemStack> modItems = new HashSet<>();

  static {
    modItems.add(ItemHumorCard.DEFAULTSTACK = new ItemStack(new ItemHumorCard()));
    modItems.add(ItemDgramCard.DEFAULTSTACK = new ItemStack(new ItemDgramCard()));
  }

  public static void init() {
    Driver.add(HumorCardDriver.driver);
    Driver.add(DgramCardDriver.driver);
  }

  @SubscribeEvent
  public static void addItems(RegistryEvent.Register<Item> event) {
    for(ItemStack itemStack : modItems) {
      event.getRegistry().register(itemStack.getItem());
    }
  }

  @SubscribeEvent
  public static void registerRenders(ModelRegistryEvent event) {
    for(ItemStack itemStack : modItems) {
      Item item = itemStack.getItem();
      ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName().toString()));
    }
  }
}
