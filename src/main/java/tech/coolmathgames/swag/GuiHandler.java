package tech.coolmathgames.swag;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import tech.coolmathgames.swag.component.CardBoxDriver;
import tech.coolmathgames.swag.driver.ContainerCardBox;
import tech.coolmathgames.swag.gui.CardBoxGUI;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {
  @Nullable
  public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity tileEntity = world.getTileEntity(new BlockPos(x, this.fixY(y), z));
    return this.getContainer(player, tileEntity);
  }

  @Nullable
  public Object getContainer(EntityPlayer player, TileEntity tileEntity) {
    if(tileEntity instanceof CardBoxDriver) {
      return ((CardBoxDriver) tileEntity).createContainer(player.inventory, player);
    }
    return null;
  }

  @Nullable
  @Override
  public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity tileEntity = world.getTileEntity(new BlockPos(x, this.fixY(y), z));
    if(tileEntity instanceof CardBoxDriver) {
      return new CardBoxGUI((ContainerCardBox) this.getContainer(player, tileEntity));
    }
    return null;
  }

  private int fixY(int value) {
    return value & 0x00FFFFFF;
  }
}
