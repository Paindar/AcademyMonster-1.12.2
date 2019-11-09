package cn.paindar.academymonster.ability;

import cn.academy.client.render.util.ArcPatterns;
import cn.academy.client.sound.ACSounds;
import cn.lambdalib2.util.*;
import cn.lambdalib2.util.entityx.handlers.Life;
import cn.paindar.academymonster.entity.EntityMobArc;
import cn.paindar.academymonster.network.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Predicate;

import static cn.lambdalib2.util.MathUtils.lerpf;

/**
 * Created by Paindar on 2017/3/7.
 */
public class AMThunderBolt extends BaseSkill
{
    private float aoeDamage;
    private float damage;
    private static float range=20f;
    private static float aoeRange=7f;

    public AMThunderBolt(EntityMob speller, float exp)
    {
        super(speller, (int)lerpf(200,100,exp), exp, "electromaster.thunder_bolt");
        aoeDamage = lerpf(9.6f, 17.4f, exp);
        damage = lerpf(16f, 29f, exp);
    }

    @Override
    public void start()
    {
        super.start();
        RayTraceResult result = Raytrace.traceLiving(speller, range);
        Vec3d end;
        boolean hitEntity = false;
        switch(result.typeOfHit)
        {
            case BLOCK:
                end = result.hitVec;
                break;
            case ENTITY:
                end = speller.getPositionEyes(1f);
                hitEntity = true;
                break;
            default:
                end = VecUtils.lookingPos(speller, range);
        }

        Predicate<Entity> exclusion= (!hitEntity)? EntitySelectors.exclude(speller) : EntitySelectors.exclude(speller, result.entityHit);
        EntityLivingBase target = (hitEntity)? (EntityLivingBase)result.entityHit : null;
        List<Entity> aoes = WorldUtils.getEntities(
            speller.world, end.x, end.y, end.z,
            aoeRange, EntitySelectors.living().and(exclusion));

        if(target != null)
        {
            attack(target, damage);
            if(getSkillExp() > 0.2 && RandUtils.ranged(0, 1) < 0.8 ) {
                target.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), 40, 3));
            }
        }

        for(Entity e:aoes)
        {
            attack((EntityLivingBase) e, aoeDamage);

            if (getSkillExp() > 0.2 && RandUtils.ranged(0, 1) < 0.8)
            {
                ((EntityLivingBase)e).addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), 20, 3));
            }
        }
        List<Entity> list= WorldUtils.getEntities(speller, 40, EntitySelectors.player());
        for(Entity e:list)
        {
            NetworkManager.sendThunderBoltTo(speller,end,aoes,(EntityPlayerMP)e);
        }

        cooldown();
    }

    public float getMaxDistance(){return range;}

    @SideOnly(Side.CLIENT)
    public static void spawnEffect(EntityLivingBase ori,Vec3d target,List<Entity> aoes)
    {
        for(int i= 0 ;i<2;i++)
        {
            EntityMobArc mainArc = new EntityMobArc(ori, ArcPatterns.strongArc);
            mainArc.length = range;
            ori.world.spawnEntity(mainArc);
            mainArc.addMotionHandler(new Life(20));
        }

        for(Entity e:aoes)
        {
            EntityMobArc aoeArc = new EntityMobArc(ori, ArcPatterns.aoeArc);
            aoeArc.lengthFixed = false;
            aoeArc.setFromTo(target.x, target.y,target.z,
                    e.posX, e.posY + e.getEyeHeight(), e.posZ);
            aoeArc.addMotionHandler(new Life(RandUtils.rangei(15, 25)));
            ori.world.spawnEntity(aoeArc);
        }

        ACSounds.playClient(ori, "em.arc_strong", SoundCategory.HOSTILE, 0.6f);
    }
}
