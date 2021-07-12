package cn.paindar.academymonster.ability;

import cn.academy.ability.vanilla.electromaster.skill.EMDamageHelper;
import cn.lambdalib2.util.RandUtils;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.potion.PotionEffect;
import scala.collection.immutable.Vector;


import static cn.lambdalib2.util.MathUtils.lerp;

public class AMBodyIntensify extends SkillTemplate {
    public static final AMBodyIntensify Instance = new AMBodyIntensify();
    protected AMBodyIntensify() {
        super("body_intensify");
    }

    @Override
    public MonsterSkillInstance create(Entity e) {
        return new BodyIntensifyContext(e);
    }

    @Override
    public SpellingInfo fromBytes(ByteBuf buf) {
        return null;
    }
    static class BodyIntensifyContext extends MonsterSkillInstance
    {
        private final int cooldown;
        public BodyIntensifyContext(Entity ent) {
            super(AMBodyIntensify.Instance, ent);
            cooldown = (int)lerp(300, 200, getExp());
        }
        private double getProbability()
        {
            return lerp(1,2.5f,getExp());
        }
        private int getBuffTime()
        {
            return (int)lerp(40f,200f, getExp());
        }
        private int getBuffLevel()
        {
            return getExp()>0.5?2:1;
        }
        @Override
        public int execute() {
            double p = getProbability();
            int time = getBuffTime();
            int level=getBuffLevel();
            Vector<PotionEffect> vector= cn.academy.ability.vanilla.electromaster.skill.BodyIntensify.effects();
            if(speller instanceof EntityCreeper)
            {
                EMDamageHelper.powerCreeper((EntityCreeper) speller);
            }
            else if(speller instanceof EntityLivingBase)
            {
                for(int i=0;i<vector.size();i++)
                {
                    if(RandUtils.ranged(0, 1+p)<p)
                    {
                        ((EntityLivingBase)speller).addPotionEffect(cn.academy.ability.vanilla.electromaster.skill.BodyIntensify.createEffect(vector.apply(i), level, time));
                    }
                }
            }
            setDisposed();
            return cooldown;
        }

        @Override
        public void clear() {

        }
    }
}
