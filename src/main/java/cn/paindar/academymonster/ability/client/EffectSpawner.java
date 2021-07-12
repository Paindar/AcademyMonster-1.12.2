package cn.paindar.academymonster.ability.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class EffectSpawner {
    public static EffectSpawner Instance = new EffectSpawner();
    private List<Entity> readySpawning = new ArrayList<>();

    private EffectSpawner(){}

    public void addEffect(Entity e)
    {
        synchronized (this)
        {
            if(readySpawning.isEmpty())
            {
                MinecraftForge.EVENT_BUS.register(this);
            }
            readySpawning.add(e);
        }
    }

    @SubscribeEvent
    public void onRenderTickEnd(TickEvent.RenderTickEvent evt)
    {
        if(evt.phase== TickEvent.Phase.START)
        {
            synchronized (this)
            {
                for(Entity e: readySpawning)
                {
                    Minecraft.getMinecraft().world.spawnEntity(e);
                }
                readySpawning.clear();
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }

    }
}
