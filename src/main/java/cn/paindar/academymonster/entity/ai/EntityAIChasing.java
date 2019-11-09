package cn.paindar.academymonster.entity.ai;

import cn.paindar.academymonster.ability.AMBodyIntensify;
import cn.paindar.academymonster.ability.AMPenetrateTeleport;
import cn.paindar.academymonster.ability.BaseSkill;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by Paindar on 2017/5/14.
 */
public class EntityAIChasing extends EntityAIBaseX
{
    EntityLivingBase target;
    float dist;
    EntityAIChasing(EntityLivingBase target, float dst)
    {
        super();
        this.target=target;
        this.dist=dst;
    }

    @Override
    public boolean execute(EntityMob owner)
    {
        double curDist=owner.getDistanceSq(target);
        MobSkillData data = MobSkillData.get(owner);
        if(target==null || target.isDead ||dist*dist<curDist||
                (target instanceof EntityPlayer && ((EntityPlayer)target).capabilities.isCreativeMode))
        {
            data.setAI(new EntityAIWander());
            return false;
        }
        //may it can add Vector Accelerate or other
        for(BaseSkill skill:data.list)
        {
            if(skill instanceof AMBodyIntensify && skill.canSpell())
            {
                ((AMBodyIntensify)skill).spell();
                break;
            }
            else if(skill instanceof AMPenetrateTeleport && skill.canSpell())
            {
                data.setAI(new EntityAIPenetrateTeleport(target, (AMPenetrateTeleport) skill));
                break;
            }
        }
        if(curDist<=400)
            data.setAI(new EntityAIRange(target));
        else
            return true;
        return false;
    }
}
