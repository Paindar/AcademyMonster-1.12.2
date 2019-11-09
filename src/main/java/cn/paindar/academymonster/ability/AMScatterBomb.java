package cn.paindar.academymonster.ability;

import cn.lambdalib2.util.*;
import cn.paindar.academymonster.entity.EntityMobMdBall;
import cn.paindar.academymonster.network.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

import static cn.lambdalib2.util.MathUtils.lerpf;

/**
 * Created by Paindar on 2017/3/12.
 */
public class AMScatterBomb extends BaseSkill
{
    private List<EntityMobMdBall> ballList=new ArrayList<>();
    private float damage;
    private float range;
    private float charging;
    private final float maxTime=240;
    public AMScatterBomb(EntityMob speller, float exp)
    {
        super(speller, (int)lerpf(160,240,exp), exp, "meltdowner.scatter_bomb");
        damage=lerpf(3,9,exp);
        range=lerpf(5,11,exp);
    }

    @Override
    public void start()
    {
        super.start();
        charging=0;
    }

    @Override
    public void onTick()
    {
        super.onTick();
        if(!isActivated())
            return;
        charging++;
        if(charging>20&&charging%10==0 && charging<=100)
        {
            EntityMobMdBall ball=new EntityMobMdBall(speller,2333333);
            ballList.add(ball);
            speller.world.spawnEntity(ball);
        }
        else if(charging>=maxTime)
        {
            stop();
        }
    }

    public void stop()
    {
        List<Entity> trgList= WorldUtils.getEntities(speller,range, EntitySelectors.living().and(EntitySelectors.exclude(speller)));
        Vec3d dst= VecUtils.lookingPos(speller, range);
        for(EntityMobMdBall ball:ballList)
        {
            Vec3d str=ball.getPositionVector();
            if(!trgList.isEmpty())
            {
                if(getSkillExp()>0.6)
                {
                    dst = trgList.get(RandUtils.nextInt(trgList.size())).getPositionVector();
                }
                else
                    dst = dst.rotatePitch(MathUtils.toRadians((RandUtils.nextFloat() - 0.5F) * 25))
                    .rotateYaw(MathUtils.toRadians((RandUtils.nextFloat() - 0.5F) * 25));
            }
            RayTraceResult trace = Raytrace.perform(speller.world,str,dst
                    , EntitySelectors.exclude(speller).and(EntitySelectors.living()));
            if (trace.typeOfHit == RayTraceResult.Type.ENTITY && trace.entityHit != null)
            {
                attack((EntityLivingBase) trace.entityHit, damage);
            }
            List<Entity> list= WorldUtils.getEntities(speller, 25, EntitySelectors.player());
            for(Entity e:list)
            {
                NetworkManager.sendMdRayEffectTo(str, dst, speller, (EntityPlayerMP)e);
            }
            ball.setDead();
        }
        ballList.clear();
        cooldown();
    }
    public int getBallSize(){return ballList.size();}
    @Override
    public void cooldown()
    {
        super.cooldown();
        clear();
    }

    public float getMaxDistance(){return range;}

    @Override
    public void clear() {
        super.clear();
        for(EntityMobMdBall ball:ballList)
        {
            ball.setDead();
        }
    }
}
