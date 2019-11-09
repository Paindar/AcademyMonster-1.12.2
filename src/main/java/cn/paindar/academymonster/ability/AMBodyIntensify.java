package cn.paindar.academymonster.ability;

import cn.academy.ability.vanilla.electromaster.skill.BodyIntensify;
import cn.academy.ability.vanilla.electromaster.skill.EMDamageHelper;
import cn.lambdalib2.util.RandUtils;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.potion.PotionEffect;
import scala.collection.immutable.Vector;

import static cn.lambdalib2.util.MathUtils.lerpf;


/**
 * Created by Paindar on 2017/2/10.
 */
public class AMBodyIntensify extends BaseSkill
{
    public AMBodyIntensify(EntityMob speller, float exp)
    {
        super(speller, (int)lerpf(300, 200,exp), exp,"electromaster.body_intensify");
    }

    private double getProbability()
    {
        return lerpf(1,2.5f,getSkillExp());
    }
    private int getBuffTime()
    {
        return (int)lerpf(40f,200f, getSkillExp());
    }
    private int getBuffLevel()
    {
        return getSkillExp()>0.5?2:1;
    }


    public void spell()
    {
        super.start();
        double p = getProbability();
        int time = getBuffTime();
        int level=getBuffLevel();
        Vector<PotionEffect> vector= BodyIntensify.effects();
        if(speller instanceof EntityCreeper)
        {
            EMDamageHelper.powerCreeper((EntityCreeper) speller);
        }
        for(int i=0;i<vector.size();i++)
        {
            if(RandUtils.ranged(0, 1+p)<p)
            {
                speller.addPotionEffect(BodyIntensify.createEffect(vector.apply(i), level, time));
            }
        }
        cooldown();
    }


}
