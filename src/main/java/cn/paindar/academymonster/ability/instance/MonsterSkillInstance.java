package cn.paindar.academymonster.ability.instance;

import cn.paindar.academymonster.ability.SkillTemplate;
import cn.paindar.academymonster.ability.api.MobSkillDamageSource;
import cn.paindar.academymonster.ability.api.event.CalcEventMob;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraftforge.common.MinecraftForge;

public abstract class MonsterSkillInstance {
    protected final SkillTemplate template;
    protected final Entity speller;
    protected final double exp;
    private boolean disposed = false;

    public MonsterSkillInstance(SkillTemplate template, Entity ent)
    {
        this.template = template;
        this.speller = ent;
        this.exp = MobSkillData.get((EntityMob) ent).getSkillData().getSkillExp(template);
    }

    //return how many ticks should be wait until be spellable
    public abstract int execute();
    public double getExp() { return exp; }
    public boolean isDisposed(){return disposed;}
    protected void setDisposed(){disposed = true;}

    private double getFinalDamage(double damage) {
        return damage;
    }

    public boolean attack(EntityLivingBase target, double damage, boolean ignoreArmor)
    {
        CalcEventMob.MobSkillAttack evt = new CalcEventMob.MobSkillAttack((EntityMob) speller, this.template, target, damage);
        boolean result = MinecraftForge.EVENT_BUS.post(evt);
        damage = evt.value;
        if (damage > 0 && !result)
        {
            double delta = (evt.sourceEnhancement-evt.targetEnhancement)/1000;
            if(delta > 3e-3)
                damage = (float)(1f + (1.7689*delta*delta))*damage;
            else if(delta < -3e-3)
                damage = (float)(1f - (2.25  *delta*delta))*damage;
            if(ignoreArmor)
                target.attackEntityFrom(new MobSkillDamageSource(speller, this.template).setDamageBypassesArmor(), (float) getFinalDamage(damage));
            else
                target.attackEntityFrom(new MobSkillDamageSource( speller, this.template), (float) getFinalDamage(damage));
        }
        return true;
    }
    public void tick(){};
    public abstract void clear();
}
