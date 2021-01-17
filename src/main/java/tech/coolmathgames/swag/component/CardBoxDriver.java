package tech.coolmathgames.swag.component;

import li.cil.oc.api.Driver;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.network.*;
import li.cil.oc.integration.opencomputers.Item$;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import tech.coolmathgames.swag.OpenSwag;
import tech.coolmathgames.swag.driver.ContainerCardBox;
import tech.coolmathgames.swag.item.BlockCardBox;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class CardBoxDriver extends TileEntityLockableLoot implements SidedEnvironment, ITickable, IInventory, EnvironmentHost {
  public NonNullList<ItemStack> contents;
  private int size = 6; // Many
  private List<ManagedEnvironment> components;
  private int numPlayersUsing;
//  private int numPlayersUsing;

  public CardBoxDriver() {
    super();
    this.contents = NonNullList.withSize(size, ItemStack.EMPTY);
    this.components = new LinkedList();
    for(int i = 0; i < size; ++i) {
      this.components.add(null);
    }
  }

  @Override
  protected NonNullList<ItemStack> getItems() {
    return contents;
  }

  @Override
  public int getSizeInventory() {
    return this.getItems().size();
  }

  @Override
  public boolean isEmpty() {
    for (ItemStack itemstack : this.contents) {
      if (!itemstack.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int getInventoryStackLimit() {
    return 1; // Maybe this could be more??? :P
  }

  @Override
  public boolean isUsableByPlayer(EntityPlayer entityplayer)
  {
    if (this.getWorld() == null) {
      return true;
    }
    if (this.getWorld().getTileEntity(pos) != this) {
      return false;
    }
    return entityplayer.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64D;
  }

  @Override
  public Container createContainer(InventoryPlayer inventoryPlayer, EntityPlayer entityPlayer) {
    return new ContainerCardBox(inventoryPlayer, this);
  }

  @Override
  public String getGuiID() {
    return OpenSwag.MODID + ":" + BlockCardBox.NAME;
  }

  @Override
  public String getName() {
    return this.hasCustomName() ? this.customName : OpenSwag.MODID + ".card_box.name";
  }

  @Override
  public void openInventory(EntityPlayer player) {
    if(!player.isSpectator()) {
      if(this.world == null) {
        return;
      }
      if (this.numPlayersUsing < 0) {
        this.numPlayersUsing = 0;
      }
      ++this.numPlayersUsing;
      this.getWorld().addBlockEvent(this.pos, BlockCardBox.DEFAULTITEM, 1, this.numPlayersUsing);
    }
  }

  // Urgghhh!!
  @Override
  public void update() {
    for(ManagedEnvironment component : this.components) {
      if (component != null && component.node() != null) {
        if (component.node().network() == null) {
          Network.joinOrCreateNetwork(this);
        }
        if(component.canUpdate()) {
          component.update();
        }
      }
    }
  }

  @Override
  public void readFromNBT(NBTTagCompound compound)
  {
    super.readFromNBT(compound);

    this.contents = NonNullList.<ItemStack> withSize(this.getSizeInventory(), ItemStack.EMPTY);

    if (compound.hasKey("CustomName", Constants.NBT.TAG_STRING)) {
      this.customName = compound.getString("CustomName");
    }

    if (!this.checkLootAndRead(compound)) {
      ItemStackHelper.loadAllItems(compound, this.contents);
    }

    // OC nodes
    for(int i = 0; i < this.size; ++i) {
      ManagedEnvironment component = this.components.get(i);
      if(component != null && component.node() != null) {
        component.node().remove();
      }
      
      ItemStack stack = this.contents.get(i);

      component = null;
      // If there's an item in the slot, create it's component:
      if(stack != null && !stack.isEmpty()) {
        component = this.onItemAdded(i, stack);
      }
      // Set the component, if there is one
      this.components.set(i, component);
    }
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound)
  {
    super.writeToNBT(compound);

    // Store nodes
    for(int i = 0; i < this.components.size(); ++i) {
      ManagedEnvironment component = this.components.get(i);
      if(component != null) {
        ItemStack itemStack = this.getItems().get(i);
        // Shouldn't be possible...
        assert(Driver.driverFor(itemStack) != null);
        NBTTagCompound tag = this.getTag(Driver.driverFor(itemStack), itemStack);
        component.save(tag);
      }
    }

    // Save compound *after* we've modified component ItemStacks!
    if (!this.checkLootAndWrite(compound)) {
      ItemStackHelper.saveAllItems(compound, this.getItems());
    }

    if (this.hasCustomName()) {
      compound.setString("CustomName", this.customName);
    }
    return compound;
  }

  private NBTTagCompound getTag(DriverItem driver, ItemStack stack) {
    NBTTagCompound nodeNbt = driver.dataTag(stack);
    if(nodeNbt == null) {
      // Scala funnies
      nodeNbt = Item$.MODULE$.dataTag(stack);
    }
    return nodeNbt;
  }

  public ManagedEnvironment onItemAdded(int index, ItemStack stack) {
    ManagedEnvironment component = this.components.get(index);
    if(component != null) {
      component.node().remove();
      component = null;
    }
    DriverItem driver = Driver.driverFor(stack);
    if(driver != null) {
      component = driver.createEnvironment(stack, this);
      if(component != null) {
        component.load(this.getTag(driver, stack));

        if (component.node().network() == null) {
          Network.joinOrCreateNetwork(this);
        }
        // If we're not reachable... Cheat!
        if(component.node().reachability().ordinal() < Visibility.Network.ordinal()) {
          try {
            // li.cil.oc.server.network.Node.class
            Field reachabilityField = component.node().getClass().getDeclaredField("reachability");
            reachabilityField.setAccessible(true);
            reachabilityField.set(component.node(), Visibility.Network);
          } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
          }
        }
        // Set our visibility to network. Technically messy because it means multiple computers can be bound to one component
        if(component.node() instanceof Component && ((Component) component.node()).visibility().ordinal() < Visibility.Network.ordinal()) {
          ((Component) component.node()).setVisibility(Visibility.Network);
        }
      }
    }
    this.components.set(index, component);
    return component;
  }

  public void onItemRemoved(int index, ItemStack stack) {
    ManagedEnvironment component = this.components.get(index);
    if(component != null && component.node() != null) {
      component.node().remove();
      if(component.node() instanceof Component) {
        component.onDisconnect(component.node());
      }
    }
    this.components.set(index, null);
  }

  @Override
  public void setInventorySlotContents(int slot, ItemStack stack) {
    if(slot >= 0 && slot < this.getSizeInventory()) {
      ItemStack existing = this.getItems().get(slot);
      if (stack.isEmpty() && existing.isEmpty() || existing == stack) {
        return;
      }
      if (!existing.isEmpty()) {
        this.onItemRemoved(slot, stack);
      }
      this.getItems().set(slot, stack);
      if (!stack.isEmpty()) {
        this.onItemAdded(slot, stack);
      }
      this.markDirty();
    }
  }

  @Override
  public ItemStack removeStackFromSlot(int slot) {
    if(slot >= 0 && slot < this.getSizeInventory()) {
      ItemStack existing = this.getItems().get(slot);
      if(!existing.isEmpty()) {
        this.onItemRemoved(slot, existing);
      }
    }
    return super.removeStackFromSlot(slot);
  }

  @Override
  public ItemStack decrStackSize(int slot, int amount) {
    if(slot >= 0 && slot < this.getSizeInventory()) {
      ItemStack existing = this.getItems().get(slot);
      if (!existing.isEmpty() && amount >= existing.getCount()) {
        this.onItemRemoved(slot, existing);
      }
    }
    return super.decrStackSize(slot, amount);
  }

  @Override
  public Node sidedNode(EnumFacing side) {
    ManagedEnvironment component = this.components.get(side.getIndex());
    return component == null ? null : component.node();
  }

  @Override
  public boolean canConnect(EnumFacing side) {
    return true;
  }

  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    DriverItem driver = Driver.driverFor(stack);
    return driver != null && driver.slot(stack) == Slot.Card;
  }

  @Override
  public World world() {
    return this.getWorld();
  }

  @Override
  public double xPosition() {
    return this.getPos().getX() + 0.5;
  }

  @Override
  public double yPosition() {
    return this.getPos().getY() + 0.5;
  }

  @Override
  public double zPosition() {
    return this.getPos().getZ() + 0.5;
  }

  @Override
  public void markChanged() {
    super.markDirty();
    this.getWorld().notifyNeighborsOfStateChange(this.getPos(), this.getBlockType(), false);
  }

  @Override
  public void invalidate() {
    super.invalidate();
    for(ManagedEnvironment component : this.components) {
      if(component != null && component.node() != null) {
        component.node().remove();
      }
    }
  }
}
