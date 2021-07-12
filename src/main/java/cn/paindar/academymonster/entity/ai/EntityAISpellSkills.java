package cn.paindar.academymonster.entity.ai;

import cn.paindar.academymonster.ability.*;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import cn.paindar.academymonster.entity.datapart.MonsterSkillList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EntityAISpellSkills extends EntityAIBase
{
    /** The entity the AI instance has been applied to */
    private final EntityMob entityHost;
    private EntityLivingBase attackTarget;
    /**
     * A decrementing tick that spawns a ranged attack once this value reaches 0. It is then set back to the
     * maxRangedAttackTime.
     */
    private final int attackIntervalMin;
    private final float attackRadius;
    private final int maxRangedAttackTime;
    private final MonsterSkillList entitySkills;
    private final boolean canIntensify;
    private final boolean canPenetrate;
    private final boolean canAoe;
    private final double peneDistSq;
    private final double maxAttackDistance;
    private int spellInterval = 0;
    public static final List<SkillTemplate> rangeSkill = new ArrayList<>();
    static {
        rangeSkill.add(AMArcGen.Instance);
        rangeSkill.add(AMElectronBomb.Instance);
        rangeSkill.add(AMElectronMissile.Instance);
        rangeSkill.add(AMLocManip.Instance);
        rangeSkill.add(AMThunderBolt.Instance);
    }
    public EntityAISpellSkills(EntityLivingBase attacker, double movespeed, int maxAttackTime, float maxAttackDistanceIn)
    {
        this(attacker, movespeed, maxAttackTime, maxAttackTime, maxAttackDistanceIn);
    }

    public EntityAISpellSkills(EntityLivingBase attacker, double movespeed, int p_i1650_4_, int maxAttackTime, float maxAttackDistanceIn)
    {
        this.spellInterval = -1;
        if (!(attacker instanceof EntityMob))
        {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        }
        else
        {
            MobSkillData entityData = MobSkillData.get((EntityMob) attacker);
            entitySkills = entityData.getSkillData();
            double exp = entitySkills.getSkillExp(AMPenetrateTeleport.Instance);
            canIntensify = entitySkills.getSkillExp(AMBodyIntensify.Instance)>1e-6;
            canAoe = entitySkills.getSkillExp(AMRailgun.Instance)>1e-6 ||
                    entitySkills.getSkillExp(AMPlasmaCannon.Instance)>1e-6;
            if(exp >= 1e-6)
            {
                canPenetrate = true;
                double peneDist = AMPenetrateTeleport.Instance.getMaxDistance(exp);
                peneDistSq = peneDist * peneDist ;
            }
            else
            {
                canPenetrate = false;
                peneDistSq = 0;
            }
            this.entityHost = (EntityMob)attacker;
            this.maxRangedAttackTime = maxAttackTime;
            this.attackIntervalMin = p_i1650_4_;
            this.attackRadius = maxAttackDistanceIn;
            this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
            this.setMutexBits(0);
        }
    }
    @Override
    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = this.entityHost.getAttackTarget();

        if (entitylivingbase == null)
        {
            return false;
        }
        else
        {
            this.attackTarget = entitylivingbase;
            return true;
        }
    }

    @Override
    public void resetTask()
    {
        this.attackTarget = null;
        this.spellInterval = -1;
    }

    @Override
    public void updateTask()
    {
        double distanceSq = this.entityHost.getDistanceSq(this.attackTarget.posX, this.attackTarget.getEntityBoundingBox().minY, this.attackTarget.posZ);
        boolean flag = this.entityHost.getEntitySenses().canSee(this.attackTarget);

        if(canPenetrate)
        {
            if(distanceSq<peneDistSq && entitySkills.getCooldown(AMPenetrateTeleport.Instance)==0)
            {
                AMPenetrateTeleport.PenetrateTeleportContext instance = (AMPenetrateTeleport.PenetrateTeleportContext)
                        entitySkills.execute(AMPenetrateTeleport.Instance, this.entityHost);
                instance.startTeleport(this.attackTarget.posX, this.attackTarget.posY, this.attackTarget.posZ);
            }
        }
        if(canIntensify && entitySkills.getCooldown(AMBodyIntensify.Instance)==0)
        {
            entitySkills.execute(AMBodyIntensify.Instance, this.entityHost);
        }

        this.entityHost.getLookHelper().setLookPositionWithEntity(this.attackTarget, 0F, 0F);

        if (--this.spellInterval == 0)
        {
            if (!flag)
            {
                if(canAoe) selectSkillAoE();
                return;
            }

            float f = MathHelper.sqrt(distanceSq) / this.attackRadius;

            if(this.maxAttackDistance<=400)
            {
                if(distanceSq <= 9)
                {
                    selectSkillMelee();
                }
                this.selectSkillRange();
            }
            this.spellInterval = MathHelper.floor(f * (float)(this.maxRangedAttackTime - this.attackIntervalMin) + (float)this.attackIntervalMin);
        }
        else if (this.spellInterval < 0)
        {
            float f2 = MathHelper.sqrt(distanceSq) / this.attackRadius;
            this.spellInterval = MathHelper.floor(f2 * (float)(this.maxRangedAttackTime - this.attackIntervalMin) + (float)this.attackIntervalMin);
        }
    }

    private void selectSkillMelee()
    {
        double exp = 0;

        exp = entitySkills.getSkillExp(AMLocationTeleport.Instance);
        if(exp>1e-6 && entitySkills.getCooldown(AMLocationTeleport.Instance)==0)
        {
            entitySkills.execute(AMLocationTeleport.Instance, this.entityHost);
        }
        exp = entitySkills.getSkillExp(AMBloodRetro.Instance);
        if(exp>1e-6 && entitySkills.getCooldown(AMBloodRetro.Instance)==0)
        {
            entitySkills.execute(AMBloodRetro.Instance, this.entityHost);
        }
        exp = entitySkills.getSkillExp(AMGroundShock.Instance);
        if(exp>1e-6 && entitySkills.getCooldown(AMGroundShock.Instance)==0)
        {
            entitySkills.execute(AMGroundShock.Instance, this.entityHost);
            return;
        }


    }
    private void selectSkillRange()
    {
        SkillTemplate[] skillList = entitySkills.getSkills();
        double exp;
        for(SkillTemplate skill : rangeSkill)
        {
            exp = entitySkills.getSkillExp(skill);
            if(exp>1e-6 && entitySkills.getCooldown(skill)==0)
            {
                entitySkills.execute(skill, this.entityHost);
            }
        }
    }
    private void selectSkillAoE()
    {
        double exp = 0;
        exp = entitySkills.getSkillExp(AMRailgun.Instance);
        if(exp>1e-6 && entitySkills.getCooldown(AMRailgun.Instance)==0)
        {
            entitySkills.execute(AMRailgun.Instance, this.entityHost);
            return ;
        }
        exp = entitySkills.getSkillExp(AMPlasmaCannon.Instance);
        if(exp>1e-6 && entitySkills.getCooldown(AMPlasmaCannon.Instance)==0)
        {
            entitySkills.execute(AMPlasmaCannon.Instance, this.entityHost);
        }
    }
}
