package cn.paindar.academymonster.ability;

import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.Raytrace;
import cn.lambdalib2.util.VecUtils;
import cn.lambdalib2.util.WorldUtils;
import cn.paindar.academymonster.entity.EntityMobMdBall;
import cn.paindar.academymonster.network.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

import static cn.lambdalib2.util.MathUtils.lerpf;

/**
 * Created by Paindar on 2017/3/12.
 */
public class AMElectronMissile extends BaseSkill
{
    private List<EntityMobMdBall> ballList=new ArrayList<>();
    private final int freq;
    private final int maxTick;
    private final float range;
    private final float damage;
    private int time;
    public AMElectronMissile(EntityMob speller, float exp)
    {
        super(speller, (int)lerpf(800,400,exp), exp, "meltdowner.electron_missile");
        freq=(int)lerpf(20,10,exp);
        maxTick=(int)lerpf(100,200,exp);
        range=(int)lerpf(8,12,exp);
        damage=lerpf(5,12,exp);
    }

    @Override
    public void start()
    {
        super.start();
        time=0;
        speller.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("speed"), maxTick, 2));
    }

    @Override
    public void onTick()
    {
        super.onTick();
        if(!isActivated())
            return;
        time++;
        if(time%freq==0)
        {
            EntityMobMdBall ball=new EntityMobMdBall(speller,2333333);
            speller.world.spawnEntity(ball);
            ballList.add(ball);
        }

        if( !ballList.isEmpty())
        {
            List<Entity> list= WorldUtils.getEntities(speller,range, EntitySelectors.exclude(speller).
                    and(EntitySelectors.living()).
                    and((Entity e)-> !(e instanceof EntityPlayer && ((EntityPlayer) e).capabilities.isCreativeMode)));
            if(!list.isEmpty())
            {
                Vec3d str= new Vec3d(ballList.get(0).posX, ballList.get(0).posY, ballList.get(0).posZ);
                Vec3d dst= VecUtils.lookingPos(list.get(0),5);
                RayTraceResult trace = Raytrace.perform(speller.world, str,dst
                        , EntitySelectors.exclude(speller).and(EntitySelectors.living()));
                if (trace.typeOfHit == RayTraceResult.Type.ENTITY)
                {
                    attack((EntityLivingBase) trace.entityHit,damage);
                }

                list= WorldUtils.getEntities(speller, 25, EntitySelectors.player());
                for(Entity e:list)
                {
                    NetworkManager.sendMdRayEffectTo(str,dst,speller, (EntityPlayerMP)e);
                }
                ballList.get(0).setDead();
                ballList.remove(0);
            }
        }
        if (time>=maxTick)
            cooldown();

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
