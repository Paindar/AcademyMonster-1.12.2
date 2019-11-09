package cn.paindar.academymonster.core;

import cn.academy.ACItems;
import cn.academy.item.ItemApp;
import cn.academy.terminal.AppRegistry;
import cn.paindar.academymonster.config.AMConfig;
import cn.paindar.academymonster.core.support.terminal.AppAIMScanner;
import cn.paindar.academymonster.core.support.tile.AbilityInterfManager;
import cn.paindar.academymonster.network.NetworkManager;
import cn.paindar.academymonster.playerskill.HookLoader;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by Paindar on 2017/2/13.
 */
public class CommonProxy
{
    public void preInit(FMLPreInitializationEvent event)
    {
        AMConfig.init(event);
        if(AMConfig.getBoolean("am.general.replaceSkill",true))
            HookLoader.init();
        SkillManager.instance.initSkill();
    }

    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new GlobalEventHandle());
        MinecraftForge.EVENT_BUS.register(AbilityInterfManager.instance);

    }

    public void postInit(FMLPostInitializationEvent event)
    {
        GameRegistry.addShapedRecipe(new ResourceLocation("academymonster","aim_scanner"), null,  new ItemStack(ItemApp.getItemForApp(AppAIMScanner.instance)), "#* ",
                " . ",
                " ^ ",
                '#', ACItems.brain_component, '*', Blocks.REDSTONE_BLOCK,'.',ACItems.data_chip,'^',ACItems.info_component);
    }
}
