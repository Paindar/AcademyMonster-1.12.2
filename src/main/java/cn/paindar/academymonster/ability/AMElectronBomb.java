package cn.paindar.academymonster.ability;

import cn.lambdalib2.s11n.network.NetworkMessage;
import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.Raytrace;
import cn.lambdalib2.util.WorldUtils;
import cn.paindar.academymonster.entity.EntityMobMdBall;
import cn.paindar.academymonster.network.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

import static cn.lambdalib2.util.MathUtils.lerpf;

/**
 * Created by Paindar on 2017/2/10.
 */
public class AMElectronBomb extends BaseSkill
{
    private float maxDistance;
    private float damage;

    public AMElectronBomb(EntityMob speller, float exp) {
        super(speller,(int)lerpf(55,25, exp), exp,"meltdowner.electron_bomb");
        damage=lerpf(6, 12, exp);
        maxDistance=lerpf(7,10,exp);
    }
    public float getMaxDistance(){return maxDistance;}

    private Vec3d getDest(EntityLivingBase speller){return Raytrace.getLookingPos(speller, maxDistance).getLeft();}

    @Override
    public void start()
    {
        super.start();
        EntityMobMdBall ball = new EntityMobMdBall(speller,(int)lerpf(30,15,getSkillExp()), ball1 ->
        {
            Vec3d str= ball1.getPositionEyes(1f),
                end=getDest(speller);
            RayTraceResult trace = Raytrace.perform(speller.getEntityWorld(),str,end
                   , EntitySelectors.exclude(speller).and(EntitySelectors.living()));
           if (trace.typeOfHit == RayTraceResult.Type.ENTITY && trace.entityHit != null)
           {
               attack((EntityLivingBase) trace.entityHit, damage);
            }
            List<Entity> list= WorldUtils.getEntities(speller, 25, EntitySelectors.player());
            for(Entity e:list)
            {
                NetworkManager.sendMdRayEffectTo(str,end,speller, (EntityPlayerMP)e);
            }
       }) ;
        speller.world.spawnEntity(ball);
        cooldown();
    }

}
