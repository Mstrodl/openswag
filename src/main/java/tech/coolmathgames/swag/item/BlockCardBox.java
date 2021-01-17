package tech.coolmathgames.swag.item;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tech.coolmathgames.swag.OpenSwag;
import tech.coolmathgames.swag.component.CardBoxDriver;

public class BlockCardBox extends BaseBlock implements ITileEntityProvider {
  public static final String NAME = "card_box";
  public static BlockCardBox DEFAULTITEM;

  public BlockCardBox() {
    super(Material.IRON, NAME, 0.5f);
  }

  @Override
  public TileEntity createNewTileEntity(World world, int var2) {
    CardBoxDriver driver = new CardBoxDriver();
    driver.setWorld(world);
    return driver;
  }

  @Override
  public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float x, float y, float z) {
    if(!world.isRemote) {
      player.openGui(OpenSwag.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
      return true;
    } else {
      return false;
    }
  }
}