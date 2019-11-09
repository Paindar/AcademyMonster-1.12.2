package cn.paindar.academymonster.ability.api;

import cn.paindar.academymonster.ability.BaseSkill;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;

/**
 * Created by Paindar on 2017/2/10.
 */
public class MobSkillDamageSource extends EntityDamageSource {

    public final BaseSkill skill;

    public MobSkillDamageSource(EntityMob speller, BaseSkill skill) {
        super("am_skill", speller);
        this.skill = skill;
    }

    // Chat display
    @Override
    public ITextComponent getDeathMessage(EntityLivingBase target) {
        return new TextComponentTranslation("death.attack.ac_skill",
                target.getName(),
                this.damageSourceEntity.getName(),
                I18n.translateToLocal(skill.getUnlocalizedSkillName()));
    }

}
