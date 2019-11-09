package cn.paindar.academymonster.core;

import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.s11n.network.NetworkS11n;
import net.minecraft.item.Item;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Paindar on 17/10/19.
 */
public class RegisterEventHandle
{
    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Item> event) {
        //event.getRegistry().registerAll(AcademyMonster.globalItem.testTools);
    }

}
