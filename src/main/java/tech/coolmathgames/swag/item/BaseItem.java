package tech.coolmathgames.swag.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import tech.coolmathgames.swag.ContentRegistry;
import tech.coolmathgames.swag.OpenSwag;

public abstract class BaseItem extends Item {
  public BaseItem(String name) {
    setUnlocalizedName(OpenSwag.MODID + "." + name);
    setRegistryName(OpenSwag.MODID, name);
    setCreativeTab(ContentRegistry.creativeTab);
  }
}
