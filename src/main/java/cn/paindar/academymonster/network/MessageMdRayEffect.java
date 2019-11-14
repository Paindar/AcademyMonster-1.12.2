package cn.paindar.academymonster.network;

import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.entity.EntityMobMDRay;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

/**
 * Created by Paindar on 2017/2/10.
 */
public class MessageMdRayEffect extends MessageAutos11n
{
    @Override
    public boolean execute() {
        if (mob==null)
        {
            AcademyMonster.log.warn("<ArcGen>Fail to find entity whose id is "+nbt.getInteger("id"));
            return true;
        }
        EntityMobMDRay raySmall  = new EntityMobMDRay(mob, str, end);
        raySmall.viewOptimize = false;
        mob.world.spawnEntity(raySmall);
        return true;
    }

    public static class H extends Handler<MessageMdRayEffect> { }
    public NBTTagCompound nbt;
    public Vec3d str, end;
    EntityMob mob;

    public MessageMdRayEffect(){}

    public MessageMdRayEffect(Vec3d str, Vec3d end, EntityMob spawner) {
        this.str = str;
        this.end = end;
        this.mob = spawner;
    }
}
