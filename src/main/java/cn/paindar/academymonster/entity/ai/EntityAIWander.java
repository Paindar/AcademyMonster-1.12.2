package cn.paindar.academymonster.entity.ai;

import cn.paindar.academymonster.entity.datapart.MobSkillData;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by Paindar on 2017/5/14.
 */
public class EntityAIWander extends EntityAIBaseX
{
    public EntityAIWander()
    {
        super();
    }

    @Override
    public boolean execute(EntityMob owner)
    {

        EntityLivingBase target=owner.getAttackTarget();

        if(target instanceof EntityPlayer && ((EntityPlayer)target).capabilities.isCreativeMode)
        {
            target=null;
        }
        if(target!=null)
        {
            MobSkillData data=MobSkillData.get(owner);
            if(owner.getDistanceSq(target)>225)
            {
                data.setAI(new EntityAIChasing(target,30));
                return false;
            }
            else
            {
                data.setAI(new EntityAIRange(target));
                return false;
            }
        }
        return true;
    }
}
