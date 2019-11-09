package cn.paindar.academymonster.entity.ai;

import cn.lambdalib2.util.BlockSelectors;
import cn.lambdalib2.util.Raytrace;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

/**
 * Created by Paindar on 2017/5/13.
 */
public abstract class EntityAIBaseX
{
    EntityAIBaseX()
    {
    }

    public abstract boolean execute(EntityMob owner);

    boolean isTargetInHorizon(EntityMob owner, EntityLivingBase target)
    {
        Vec3d lookingPos=owner.getLookVec().normalize(),
                direct=new Vec3d(target.posX-owner.posX,target.posY-owner.posY,target.posZ-owner.posZ).normalize();

        RayTraceResult trace = Raytrace.rayTraceBlocks(owner.getEntityWorld(),
                owner.getPositionEyes(1f),
                target.getPositionEyes(1f), BlockSelectors.filNothing
        );
        return (lookingPos.x*direct.x+lookingPos.z*direct.z>=0.5) &&
                (trace==null || trace.typeOfHit!= RayTraceResult.Type.BLOCK);
    }

    boolean isTargetInHorizonIgnoreBlock(EntityLivingBase owner, EntityLivingBase target)
    {
        Vec3d lookingPos=owner.getLookVec().normalize(),
                direct=new Vec3d(target.posX-owner.posX,target.posY-owner.posY,target.posZ-owner.posZ).normalize();
        return (lookingPos.x*direct.x+lookingPos.z*direct.z>=0.5);
    }
}
