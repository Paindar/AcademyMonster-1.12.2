package cn.paindar.academymonster.ability;

import cn.lambdalib2.util.SideUtils;
import cn.paindar.academymonster.ability.api.MobSkillDamageSource;
import cn.paindar.academymonster.ability.api.event.CalcEventMob;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Created by Paindar on 2017/2/9.
 */

public abstract class BaseSkill
{
    private int maxCooldown;
    private int remainCooldown=0;
    private float skillExp;
    protected EntityMob speller;
    private String skillName;
    boolean activate=false;
    private int interfered = 0;
    public BaseSkill(){}
    public BaseSkill(EntityMob speller,int maxCooldown,float abilityExp,String name)
    {
        this.maxCooldown=maxCooldown;
        this.skillExp=abilityExp;
        this.speller=speller;
        this.skillName=name;
    }

    public float getSkillExp(){return skillExp;}

    public boolean isInterf(){return interfered>0;}
    public boolean isSkillInCooldown(){return remainCooldown!=0;}

    /**
     * checked if skill can spell.
     * Type:
     * Cooldown Chant   Interfer    Result
     * F        F       F           Valid(True)
     * F        F       T           Interfered(False)
     * F        T       F           Chanting(False)
     * F        T       T           Interrupted(False, impossible)
     * T        F       F           Cooldown(False)
     * T        F       T           Interfered(False)
     * T        T       F           Impossible(Error)
     * T        T       T           Impossible(Error)
     * @return nothing
     */
    public boolean canSpell()
    {
        return !isSkillInCooldown()&& !activate &&!isInterf();
    }

    private float getFinalDamage(float damage) {
        return damage;
    }

    public boolean isActivated(){return activate;}

    public void onTick()
    {
        if (interfered > 0)
            interfered--;
        if (remainCooldown > 0)
            remainCooldown--;
    }

    public void start()
    {
        activate = true;
    }
    void cooldown(){remainCooldown = maxCooldown;activate=false;}
    public void addInterfering(int tick)
    {
        interfered = tick;
        if(activate)
            cooldown();
    }

    public boolean attack(EntityLivingBase target,float damage)
    {
        damage = CalcEventMob.calc(new CalcEventMob.MobSkillAttack(speller, this, target, damage));

        if (damage > 0)
        {
            target.attackEntityFrom(new MobSkillDamageSource(speller, this), getFinalDamage(damage));
        }
        return true;
    }

    public boolean attackIgnoreArmor(EntityLivingBase target,float damage)
    {
        damage = CalcEventMob.calc(new CalcEventMob.MobSkillAttack(speller, this, target, damage));

        if (damage > 0)
        {
            target.attackEntityFrom(new MobSkillDamageSource(speller, this).setDamageBypassesArmor(), getFinalDamage(damage));
        }
        return true;
    }

    public String getUnlocalizedSkillName(){return "ac.ability." + skillName + ".name";}
    public String getSkillName(){return I18n.translateToLocal(skillName);}

    public void clear(){}

    public void toBytes(ByteBuf buf)
    {
        buf.writeShort(speller.dimension);
        buf.writeInt(speller.getEntityId());
        buf.writeInt(maxCooldown);
        buf.writeFloat(skillExp);
        ByteBufUtils.writeUTF8String(buf, skillName);
    }
}
