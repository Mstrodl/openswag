package tech.coolmathgames.swag;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import li.cil.oc.api.Driver;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import tech.coolmathgames.swag.driver.ComputeCardDriver;
import tech.coolmathgames.swag.item.BlockCardBox;
import tech.coolmathgames.swag.item.ItemComputeCard;

import javax.annotation.Nonnull;
import java.util.HashSet;

@Mod.EventBusSubscriber
public class ContentRegistry {
  public static CreativeTabs creativeTab = new CreativeTabs("tabOpenSwag") {
    public @Nonnull ItemStack getTabIconItem() {
      return ItemComputeCard.DEFAULTSTACK;
    }

    public @Nonnull String getTranslatedTabLabel() {
      return new TextComponentTranslation("itemGroup.OpenSwag.tabOpenSwag").getUnformattedText();
    }
  };

  public static final HashSet<ItemStack> modItems = new HashSet<>();
  public static final HashSet<Block> modBlocks = new HashSet<>();

  static {
    modItems.add(ItemComputeCard.DEFAULTSTACK = new ItemStack(new ItemComputeCard()));
//    modBlocks.add(BlockCardBox.DEFAULTITEM = new BlockCardBox());
  }

  public static void init() {
    Driver.add(ComputeCardDriver.driver);
  }

  @SubscribeEvent
  public static void addItems(RegistryEvent.Register<Item> event) {
    for(ItemStack itemStack : modItems) {
      event.getRegistry().register(itemStack.getItem());
    }
  }

  @SubscribeEvent
  public static void addBlocks(RegistryEvent.Register<Block> event) {
    for(Block block : modBlocks) {
      event.getRegistry().register(block);
    }
  }

  @SubscribeEvent
  public static void registerRenders(ModelRegistryEvent event) {
    for(ItemStack itemStack : modItems) {
      Item item = itemStack.getItem();
      ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName().toString()));
    }
//    for(Block block : modBlocks) {
//      ModelLoader.setCustomModelResourceLocation(block.DEFAULTITEM, 0, new ModelResourceLocation(block.DEFAULTITEM.getRegistryName().toString()));
//    }
  }

  private static void registerTileEntity(Class<? extends TileEntity> tileEntityClass, String key) {
    GameRegistry.registerTileEntity(tileEntityClass, new ResourceLocation(OpenSwag.MODID, key));
  }
}
