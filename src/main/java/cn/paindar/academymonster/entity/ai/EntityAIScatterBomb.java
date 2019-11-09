package cn.paindar.academymonster.entity.ai;

import cn.paindar.academymonster.ability.AMScatterBomb;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by Paindar on 2017/3/12.
 */
public class EntityAIScatterBomb extends EntityAIBaseX
{
    private EntityLivingBase target;
    private AMScatterBomb skill;

    EntityAIScatterBomb(EntityLivingBase target,AMScatterBomb skill)
    {
        super();
        this.target=target;
        this.skill=skill;
    }

    @Override
    public boolean execute(EntityMob owner)
    {
        MobSkillData data= MobSkillData.get(owner);
        if(skill.isSkillInCooldown())
        {
            data.setAI(new EntityAIRange(target));
        }
        if(target!=null)
        {
            if(target.isDead||(target instanceof EntityPlayer && ((EntityPlayer)target).capabilities.isCreativeMode))
            {
                if(skill.isActivated())
                {
                    skill.cooldown();
                }
                data.setAI(new EntityAIWander());
            }
            if(skill.canSpell())
            {
                skill.start();
            }
            else
            {
                double range = owner.getDistanceSq(target);
                if (range <= skill.getMaxDistance() * skill.getMaxDistance())
                {
                    if(skill.getBallSize()>=7)
                    {
                        skill.stop();
                        data.setAI(new EntityAIRange(target));
                    }
                }
            }
        }
        else
        {
            data.setAI(new EntityAIWander());
            return false;
        }
        return true;
    }
}
