package cn.paindar.academymonster.entity;

import cn.academy.entity.EntityBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Created by Paindar on 2017/6/4.
 */
public class EntityBlockNative extends EntityBlock
{

    public EntityBlockNative()
    {
        super((EntityPlayer)null);
    }

    public EntityBlockNative(World world)
    {
        super(world);
    }

}
