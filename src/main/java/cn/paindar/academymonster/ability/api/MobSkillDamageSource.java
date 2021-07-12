package cn.paindar.academymonster.ability.api;

import cn.paindar.academymonster.ability.SkillTemplate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * Created by Paindar on 2017/2/10.
 */
public class MobSkillDamageSource extends EntityDamageSource {

    public final SkillTemplate skill;

    public MobSkillDamageSource(Entity speller, SkillTemplate skill) {
        super("am_skill", speller);
        this.skill = skill;
    }

    // Chat display
    @Override
    public ITextComponent getDeathMessage(EntityLivingBase target) {
        return new TextComponentTranslation("death.attack.ac_skill",
                target.getName(),
                this.damageSourceEntity.getName(),
                skill.getSkillName());
    }

}
