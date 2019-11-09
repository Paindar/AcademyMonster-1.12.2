package cn.paindar.academymonster.core;

import cn.academy.client.render.util.ArcPatterns;
import cn.paindar.academymonster.core.support.terminal.ui.AIMScannerUI;
import cn.paindar.academymonster.entity.EntityMobArc;
import net.minecraftforge.fml.common.event.*;

/**
 * Created by Paindar on 2017/2/13.
 */
public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
        //BossHealthBar.preInit();
        EntityMobArc.defaultPatternsInit();
        AcademyMonster.log.info("Init arc patterns, size "+ ArcPatterns.weakArc.length);
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        //ClientRegistry.registerKeyBinding(showTime);
        super.init(event);

    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
    }
}
