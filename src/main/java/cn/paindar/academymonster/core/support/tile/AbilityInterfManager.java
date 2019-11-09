package cn.paindar.academymonster.core.support.tile;

import cn.academy.block.TileAbilityInterferer;
import cn.academy.block.block.BlockAbilityInterferer;
import cn.lambdalib2.util.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

/**
 * Created by Paindar on 2017/3/23.
 */
public class AbilityInterfManager
{
    private Set<Entity> affectEntity=new LinkedHashSet<>();
    private int maxTick=5;
    private int tick=0;
    public static AbilityInterfManager instance=new AbilityInterfManager();
    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START && tick++==maxTick)
        {
            update();
            tick=0;
        }
    }

    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.PlaceEvent event)
    {
        if(event.getPlacedBlock() instanceof BlockAbilityInterferer)
        {
            BlockPos pos =event.getPos();
            AMWorldData data=AMWorldData.get(event.getWorld());
            data.set.add(pos);
            data.markDirty();
        }
    }

    private void update()
    {
        WorldServer[] worlds= DimensionManager.getWorlds();
        affectEntity.clear();
        for(WorldServer world:worlds)
        {
            if(world==null)
                continue;
            Set<BlockPos> set=AMWorldData.get(world).set;
            for(BlockPos pos:set)
            {
                TileEntity tile=world.getTileEntity(pos);
                if(tile instanceof TileAbilityInterferer)
                {
                    TileAbilityInterferer tAI=(TileAbilityInterferer)tile;
                    if(tAI.enabled())
                    {
                        List<Entity> list= WorldUtils.getEntities(tAI,tAI.range(),(Entity e)->((e instanceof IMob)&& !e.isDead));
                        affectEntity.addAll(list);
                    }
                }
                else
                    set.remove(pos);
            }
        }
    }

    public boolean find(Entity e){return affectEntity.contains(e);}

}
