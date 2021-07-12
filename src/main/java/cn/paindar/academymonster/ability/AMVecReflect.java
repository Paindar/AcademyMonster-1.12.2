package cn.paindar.academymonster.ability;

import cn.academy.ability.vanilla.vecmanip.skill.EntityAffection;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.util.Raytrace;
import cn.lambdalib2.util.VecUtils;
import cn.lambdalib2.util.WorldUtils;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import cn.paindar.academymonster.entity.datapart.MonsterSkillList;
import cn.paindar.academymonster.events.RayShootingEvent;
import io.netty.buffer.ByteBuf;
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
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

import static cn.lambdalib2.util.MathUtils.lerp;
import static cn.lambdalib2.util.VecUtils.*;

public class AMVecReflect extends SkillTemplate
{
    public static final AMVecReflect Instance = new AMVecReflect();

    private static final VecReflectTrigger Trigger = new VecReflectTrigger();
    protected AMVecReflect()
    {
        super("vec_reflect");
    }
    @Override
    public MonsterSkillInstance create(Entity e) {
        return new VecReflectContext(e);
    }

    @Override
    public SpellingInfo fromBytes(ByteBuf buf) {
        return null;
    }

    static class VecReflectTrigger
    {
        @StateEventCallback
        public static void init(FMLPostInitializationEvent evt)
        {
            MinecraftForge.EVENT_BUS.register(Trigger);
        }

        private boolean hasSkill(Entity e)
        {
            if(e instanceof EntityMob)
            {
                MobSkillData data = MobSkillData.get((EntityMob) e);
                MonsterSkillList list = data.getSkillData();
                return list.getSkillExp(Instance) >= 1e-6 && list.getCooldown(Instance) == 0;
            }
            return false;
        }

        @SubscribeEvent
        public void onReflect(RayShootingEvent evt)
        {
            if(!(evt.target instanceof EntityMob))
                return;
            MobSkillData data = MobSkillData.get((EntityMob) evt.target);
            MonsterSkillList list = data.getSkillData();
            if(list.getSkillExp(Instance) >= 1e-6 && list.getCooldown(Instance) == 0)
            {
                VecReflectContext ctx = (VecReflectContext) list.execute(Instance, evt.target);
                ctx.onReflect(evt);
            }

        }
        @SubscribeEvent
        public void onLivingAttack(LivingAttackEvent evt)
        {
            if(!(evt.getEntity() instanceof EntityMob))
                return;
            MobSkillData data = MobSkillData.get((EntityMob) evt.getEntity());
            MonsterSkillList list = data.getSkillData();
            if(list.getSkillExp(Instance) >= 1e-6 && list.getCooldown(Instance) == 0)
            {
                VecReflectContext ctx = (VecReflectContext) list.execute(Instance, evt.getEntity());
                ctx.onLivingAttack(evt);
            }

        }
        @SubscribeEvent
        public void onLivingHurt(LivingHurtEvent evt) {
            if(!(evt.getEntity() instanceof EntityMob))
                return;
            MobSkillData data = MobSkillData.get((EntityMob) evt.getEntity());
            MonsterSkillList list = data.getSkillData();
            if(list.getSkillExp(Instance) >= 1e-6 && list.getCooldown(Instance) == 0)
            {
                VecReflectContext ctx = (VecReflectContext) list.execute(Instance, evt.getEntity());
                ctx.onLivingHurt(evt);
            }
        }
    }

    static class VecReflectContext extends MonsterSkillInstance
    {
        private int time=0;
        private final int maxTime;
        private final double reflectRate;
        private final double maxDamage;
        private double dmg;
        private final int cooldown;
        public VecReflectContext(Entity ent) {
            super(Instance, ent);
            maxTime=(int)lerp(60,240,getExp());
            maxDamage=lerp(200,1200,getExp());
            reflectRate=lerp(0.3f,2f,getExp());
            cooldown = (int)lerp(400,300,getExp());
        }

        @Override
        public int execute() {
            time=maxTime;
            dmg=maxDamage;
            MinecraftForge.EVENT_BUS.register(this);
            return WAITING;
        }

        @Override
        public void tick() {
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
                clear();
        }

        @SubscribeEvent
        public void onReflect(RayShootingEvent evt)
        {
            if (evt.target.equals(speller)) 
            {
                dmg -= evt.range * reflectRate;
                if (dmg >= 0) {
                    evt.setCanceled(true);
                }
            }
        }
        /**
         * @param passby If passby=true, and this isn't a complete absorb, the action will not perform. Else it will.
         * @return (Whether action had been really performed, processed damage)
         */
        private double handleAttack(DamageSource dmgSource, double dmg, boolean passby)
        {
            double refDmg=0;
            double returnRatio = reflectRate;
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
                            attack((EntityLivingBase) sourceEntity, refDmg,true);
                        }
                        else
                        {
                            refDmg=this.dmg;
                            this.dmg=0;
                            attack((EntityLivingBase) sourceEntity, refDmg,false);
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
            if (evt.getEntityLiving().equals(speller)) {
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
            evt.setAmount((float) handleAttack(evt.getSource(), evt.getAmount(), false));
        }

        private static void reflect(Entity entity,Entity e)
        {
            Vec3d lookingPos = Raytrace.getLookingPos((EntityLivingBase) e, 20,null,null).getLeft();
            double speed = new Vec3d(entity.motionX, entity.motionY, entity.motionZ).length();
            Vec3d vel = VecUtils.multiply(VecUtils.subtract(lookingPos, entityHeadPos(entity)).normalize(), speed);
            setMotion(entity, vel);
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
            Vec3d lookPos = Raytrace.getLookingPos(speller, 20,null,null).getLeft();
            double speed = new Vec3d(source.motionX, source.motionY, source.motionZ).length();
            Vec3d vel = multiply(subtract(lookPos, entityHeadPos(source)).normalize(), speed);
            setMotion(fireball, vel);
            EntityAffection.mark(fireball);
            speller.world.spawnEntity(fireball);
        }

        public void clear()
        {
            MinecraftForge.EVENT_BUS.unregister(this);
            setDisposed();
            MobSkillData.get((EntityMob) speller).getSkillData().setCooldown(template, cooldown);
        }
    }
}
