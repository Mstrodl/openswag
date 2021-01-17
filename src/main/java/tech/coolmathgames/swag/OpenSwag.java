package tech.coolmathgames.swag;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
@Mod(
        modid = OpenSwag.MODID, name = "OpenSwag",
        version = BuildInfo.versionNumber + "-" + BuildInfo.buildNumber,
        dependencies = "required-after:opencomputers;")
public class OpenSwag {
  public static final String MODID = "openswag";

  @Instance(value = MODID)
  public static OpenSwag instance = new OpenSwag();

  public static final Logger logger = LogManager.getFormatterLogger(MODID);

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    ContentRegistry.init();
    NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
  }
}