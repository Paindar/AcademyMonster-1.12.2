package cn.paindar.academymonster.core;

import cn.lambdalib2.registry.RegistryMod;
import cn.paindar.academymonster.core.command.CommandTest;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * Created by Paindar on 2017/2/9.
 */

@Mod(modid =AcademyMonster.MODID , name = AcademyMonster.NAME, version = AcademyMonster.VERSION,
        dependencies = "required-after:academy@@AC_VERSION@") // LambdaLib is currently unstable. Supports only one version.
@RegistryMod(rootPackage = "cn.paindar.", resourceDomain = "academymonster")
public class AcademyMonster
{
    public static final String MODID = "academymonster";
    public static final String NAME = "Academy Monster";
    public static final String VERSION = "@VERSION@";
    public static final Logger log = LogManager.getLogger("AcademyMonster");
    @SidedProxy(clientSide = "cn.paindar.academymonster.core.ClientProxy",
            serverSide = "cn.paindar.academymonster.core.CommonProxy")
    private static CommonProxy proxy;
    @Instance
    public static AcademyMonster instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit(event);
    }
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandTest());
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
    }







}
