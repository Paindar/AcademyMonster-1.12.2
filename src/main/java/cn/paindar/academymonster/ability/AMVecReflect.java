package cn.paindar.academymonster.ability;

import cn.academy.ability.vanilla.vecmanip.skill.EntityAffection;
import cn.lambdalib2.util.Raytrace;
import cn.lambdalib2.util.VecUtils;
import cn.lambdalib2.util.WorldUtils;
import cn.paindar.academymonster.events.RayShootingEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

import static cn.lambdalib2.util.MathUtils.lerpf;
import static cn.lambdalib2.util.VecUtils.*;

/**
 * Created by Paindar on 2017/3/11.
 */
public class AMVecReflect extends BaseSkill
{
    private int time=0;
    private final int maxTime;
    private float reflectRate;
    private final float maxDamage;
    private float dmg;
    public AMVecReflect(EntityMob speller, float exp)
    {
        super(speller, (int)lerpf(400,300,exp), exp, "vecmanip.vec_reflection");
        maxTime=(int)lerpf(60,240,exp);
        maxDamage=lerpf(200,1200,exp);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void start()
    {
        super.start();
        reflectRate=lerpf(0.3f,2f,getSkillExp());
        time=maxTime;
        dmg=maxDamage;
    }

    @Override
    public void onTick()
    {
        super.onTick();
        if (!isActivated())
        {
            return;
        }
        time--;
        List<Entity> entities = WorldUtils.getEntities(speller, 5, (Entity entity) -> (!EntityAffection.isMarked(entity)));
        for (Entity entity : entities)
        {
            if (entity instanceof EntityFireball)
            {
                createNewFireball((EntityFireball) entity);
            }
            else if(!(entity instanceof EntityLivingBase))
            {
                reflect(entity, speller);
                EntityAffection.mark(entity);
            }
        }
        if(time<=0 || dmg <=0)
            cooldown();
    }

    @SubscribeEvent
    public void onReflect(RayShootingEvent evt)
    {
        if (evt.target.equals(speller)) {
            if(canSpell())
            {
                start();//make skill available
            }
            if(isActivated()) {
                dmg -= evt.range * reflectRate;
                if (dmg >= 0) {
                    evt.setCanceled(true);
                }
            }
        }
    }

    /**
     * @param passby If passby=true, and this isn't a complete absorb, the action will not perform. Else it will.
     * @return (Whether action had been really performed, processed damage)
     */
    private float handleAttack(DamageSource dmgSource, float dmg,Boolean passby)
    {
        float refDmg=0;
        float returnRatio = reflectRate;
        if (!passby)
        { // Perform the action.
            Entity sourceEntity = dmgSource.getTrueSource();

            if (sourceEntity != null && sourceEntity != speller)
            {
                if(sourceEntity instanceof EntityLivingBase)
                {
                    if(this.dmg>=returnRatio * dmg)
                    {
                        refDmg=returnRatio * dmg;
                        this.dmg-=refDmg;
                        attack((EntityLivingBase) sourceEntity, refDmg);
                    }
                    else
                    {
                        refDmg=this.dmg;
                        this.dmg=0;
                        attack((EntityLivingBase) sourceEntity, refDmg);
                    }

                }
                else
                {
                    reflect(sourceEntity, speller);
                    EntityAffection.mark(sourceEntity);
                }
            }
            return Math.max(0,dmg-refDmg);
        }
        else
        {
            refDmg = Math.min(this.dmg, returnRatio * dmg);
            this.dmg-=refDmg;
            return Math.max(0,dmg-refDmg);
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent evt)
    {
        if(!evt.getEntityLiving().equals(speller))
            return;
        if(canSpell())
        {
            start();//make skill available
        }
        if (evt.getEntityLiving().equals(speller) && isActivated()) {
            dmg-=evt.getAmount()*reflectRate;
            if ( handleAttack(evt.getSource(), evt.getAmount(),  true)<=0) {
                evt.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent evt)
    {
        if(!evt.getEntityLiving().equals(speller))
            return;
        if(canSpell())
        {
            start();
        }
        if(isActivated())
            evt.setAmount(handleAttack(evt.getSource(), evt.getAmount(), false));
    }

    private static void reflect(Entity entity,EntityLivingBase player)
    {
        Vec3d lookPos = Raytrace.getLookingPos(player, 20).getLeft();
        double speed = new Vec3d(entity.motionX, entity.motionY, entity.motionZ).length();
        Vec3d vel = VecUtils.multiply(VecUtils.subtract(lookPos, VecUtils.entityHeadPos(entity)).normalize(), speed);
        VecUtils.setMotion(entity, vel);

    }

    private void createNewFireball(EntityFireball source)
    {
        source.setDead();

        EntityLivingBase shootingEntity = source.shootingEntity;
        EntityFireball fireball;
        if(source instanceof EntityLargeFireball)
        {
            fireball = new EntityLargeFireball(((EntityLargeFireball) source).world, shootingEntity, shootingEntity.posX,
                    shootingEntity.posY, shootingEntity.posZ);
            ((EntityLargeFireball)fireball).explosionPower = ((EntityLargeFireball)source).explosionPower;
        }
        else
        {
            if(source.shootingEntity==null)
            {
                fireball = new EntitySmallFireball(source.world, source.posX, source.posY, source.posZ,
                        source.posX, source.posY, source.posZ);
            }
            else
            {
                fireball = new EntitySmallFireball(source.world, shootingEntity, shootingEntity.posX,
                        shootingEntity.posY, shootingEntity.posZ);
            }
        }
        fireball.setPosition(source.posX, source.posY, source.posZ);
        Vec3d lookPos = Raytrace.getLookingPos(speller, 20).getLeft();
        double speed = new Vec3d(source.motionX, source.motionY, source.motionZ).length();
        Vec3d vel = multiply(subtract(lookPos, entityHeadPos(source)).normalize(), speed);
        setMotion(fireball, vel);
        EntityAffection.mark(fireball);
        speller.world.spawnEntity(fireball);
    }

    @Override
    public void clear()
    {
        super.clear();
        MinecraftForge.EVENT_BUS.unregister(this);
        cooldown();
    }

}
