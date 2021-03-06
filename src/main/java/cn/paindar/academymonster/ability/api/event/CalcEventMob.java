package cn.paindar.academymonster.ability.api.event;

import cn.academy.event.ability.CalcEvent;
import cn.academy.item.armor.ACArmorHelper;
import cn.paindar.academymonster.ability.BaseSkill;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created by Paindar on 2017/2/10.
 */
public class CalcEventMob<T> extends Event {
    public T value;
    public EntityMob mob;

    public static <T> T calc(CalcEventMob<T> evt) {
        MinecraftForge.EVENT_BUS.post(evt);
        return evt.value;
    }

    public CalcEventMob(EntityMob mob, T initial) {
        value = initial;
        this.mob = mob;
    }

    public static class MobSkillAttack extends CalcEventMob<Float> {

        public final BaseSkill skill;
        public final Entity target;
        public double sourceEnhancement=0, targetEnhancement=0;

        public MobSkillAttack(EntityMob mob, BaseSkill _skill, Entity _target, float initial) {
            super(mob, initial);
            skill = _skill;
            target = _target;
            if(_target instanceof EntityPlayer)
            {
                sourceEnhancement = MobSkillData.get(mob).getEnchancement();
                targetEnhancement = ACArmorHelper.instance.getEntityEnhancement((EntityPlayer) _target);
            }
        }
    }
}
