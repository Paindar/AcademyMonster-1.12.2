package cn.paindar.academymonster.core;

import cn.academy.ACItems;
import cn.academy.ability.Skill;
import cn.academy.ability.vanilla.VanillaCategories;
import cn.academy.ability.vanilla.vecmanip.skill.VecReflection$;
import cn.academy.datapart.AbilityData;
import cn.academy.datapart.CPData;
import cn.academy.event.ability.ReflectEvent;
import cn.lambdalib2.util.RandUtils;
import cn.paindar.academymonster.entity.ai.EntityAISpellSkills;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import cn.paindar.academymonster.events.RayShootingEvent;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static cn.lambdalib2.util.MathUtils.lerpf;


/**
 * Created by Paindar on 2017/2/12.
 * A test class used to spawn AcademyEntity.
 */
public class GlobalEventHandle
{
    public GlobalEventHandle()
    {
    }

//    @SubscribeEvent
//    public void onEntityHurt(LivingHurtEvent evt)
//    {
//        if (evt.getEntity() instanceof EntityVillager)
//        {
//            evt.setAmount(0);
//        }
//    }

    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinWorldEvent event)
    {
        if(!event.getWorld().isRemote && event.getEntity() instanceof EntityMob)
        {
            EntityMob entity = (EntityMob) event.getEntity();
            MobSkillData data = MobSkillData.get(entity);
            if(!data.isLocked())
            {
                SkillManager.instance.addSkill(entity);
                data.init();
            }
            if(data.getSkillData().getSkills().length>0)
            {
                entity.tasks.addTask(2, new EntityAISpellSkills(entity,entity.getAIMoveSpeed(),10,20F));
            }
        }
    }
    /*
    @SubscribeEvent
    public void playerStartedTracking(PlayerEvent.StartTracking e) {
        if(e.getTarget() instanceof EntityMob)
        {
            MobSkillData data = MobSkillData.get((EntityMob) e.getTarget());
            if (!data.getSkillData().isEmpty())
            {
                NetworkManager.sendEntitySkillInfoTo( (EntityMob)e.getTarget(),(EntityPlayerMP) e.getEntityPlayer());
            }

        }
    }*/

    @SubscribeEvent
    public void onMonsterDied(LivingDeathEvent event)
    {
        if(! (event.getEntityLiving() instanceof EntityMob) )
            return;
        EntityMob theDead=(EntityMob)event.getEntityLiving();
        MobSkillData data=MobSkillData.get(theDead);
        data.onPlayerDead();
        if(data.level>=4)
        {
            switch(data.catalog)
            {
                case electro:
                    if(RandUtils.nextFloat()<=-0.45+data.level*0.15)
                        theDead.entityDropItem(ACItems.induction_factor.create(VanillaCategories.electromaster),1);
                    break;
                case meltdown:
                    if(RandUtils.nextFloat()<=-1+data.level*0.4)
                        theDead.entityDropItem(ACItems.induction_factor.create(VanillaCategories.meltdowner),1);
                    break;
                case teleport:
                    if(RandUtils.nextFloat()<=-1+data.level*0.4)
                        theDead.entityDropItem(ACItems.induction_factor.create(VanillaCategories.teleporter),1);
                    break;
                case vector:
                    if(RandUtils.nextFloat()<=-1+data.level*0.4)
                        theDead.entityDropItem(ACItems.induction_factor.create(VanillaCategories.vecManip),1);
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerReflectRayShoot(RayShootingEvent evt)
    {
        if(evt.target instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) evt.target;
            if(MinecraftForge.EVENT_BUS.post(new ReflectEvent(player, null, player))){
                AbilityData data = AbilityData.get(player);
                CPData cpData = CPData.get(player);
                Skill reflect = VecReflection$.MODULE$;
                float cpConsume = (float) (evt.range*lerpf(0.8f, 1.2f, data.getSkillExp(reflect)))*
                        reflect.getCPConsumeSpeed();
                if(cpData.perform(0,cpConsume))
                {
                    evt.setCanceled(true);
                }
            }
        }
    }
//    @SubscribeEvent
//    @SideOnly(Side.CLIENT)
//    public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre event)
//    {
//        BossHealthBar.flushHealthBar(event);
//    }

}
