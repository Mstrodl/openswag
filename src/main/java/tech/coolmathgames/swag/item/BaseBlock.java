package tech.coolmathgames.swag.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.EnumBlockRenderType;
import tech.coolmathgames.swag.ContentRegistry;
import tech.coolmathgames.swag.OpenSwag;

public abstract class BaseBlock extends BlockContainer {
  public static final String NAME = "base_block";
  public static Item DEFAULTITEM = null;

  public BaseBlock(Material material, String name, float hardness) {
    super(material);
    setUnlocalizedName(OpenSwag.MODID + "." + name);
    setRegistryName(OpenSwag.MODID, name);
    setCreativeTab(ContentRegistry.creativeTab);
    setHardness(hardness);
  }

  @Override
  public EnumBlockRenderType getRenderType(IBlockState state)
  {
    return EnumBlockRenderType.MODEL;
  }
}
