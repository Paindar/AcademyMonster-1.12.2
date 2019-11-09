package cn.paindar.academymonster.entity.ai;

import cn.lambdalib2.util.Raytrace;
import cn.paindar.academymonster.ability.*;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

/**
  * Created by Paindar on 2017/6/15.
  */
class EntityAIRange extends EntityAIBaseX
{
    EntityLivingBase target;
    public EntityAIRange(EntityLivingBase target)
    {
        this.target = target;
    }
    @Override
    public boolean execute(EntityMob owner) {
        double curDist = owner.getDistanceSq(target);
        MobSkillData data = MobSkillData.get(owner);
        if (target == null || target.isDead || 225 < curDist ||
                (target instanceof EntityPlayer && ((EntityPlayer) target).capabilities.isCreativeMode)) {
            data.setAI(new EntityAIWander());
            return false;
        }

        for (BaseSkill skill : data.list) {
            double validDist = .0;
            if (skill instanceof AMBodyIntensify && skill.canSpell()) {
                ((AMBodyIntensify) skill).spell();
                continue;
            }
            if (skill instanceof AMArcGen && skill.canSpell()) {
                validDist = ((AMArcGen) skill).getMaxDistance();
                if (validDist * validDist >= curDist) {
                    Vec3d lookingPos = new Vec3d(owner.getLookVec().x, 0, owner.getLookVec().z).normalize();
                    Vec3d direct = new Vec3d(target.posX - owner.posX, 0, target.posZ - owner.posZ).normalize();
                    RayTraceResult trace = Raytrace.perform(owner.getEntityWorld(),
                            owner.getPositionEyes(1f),
                            target.getPositionEyes(1f));
                    if (lookingPos.x * direct.x + lookingPos.z * direct.z >= 0.5)
                        switch (trace.typeOfHit)
                        {
                            case BLOCK:
                                break;
                            case ENTITY:
                                skill.start();
                                return true;
                        }
                }
            } else if (skill instanceof AMElectronBomb && skill.canSpell()) {
                validDist = ((AMElectronBomb) skill).getMaxDistance();
                if (validDist * validDist >= curDist) if (isTargetInHorizon(owner, target)) {
                    skill.start();
                    return true;
                }
            } else if (skill instanceof AMScatterBomb && skill.canSpell()) {
                data.setAI(new EntityAIScatterBomb(target, (AMScatterBomb) skill));
                return false;
            } else if (skill instanceof AMGroundShock && skill.canSpell()) {
                validDist = ((AMGroundShock) skill).getMaxDistance();
                if (validDist * validDist >= curDist) if (isTargetInHorizon(owner, target)) {
                    skill.start();
                    return true;
                }
            } else if (skill instanceof AMBloodRetrograde && skill.canSpell()) {
                validDist = ((AMBloodRetrograde) skill).getMaxDistance();
                if (validDist * validDist >= curDist) if (isTargetInHorizon(owner, target)) {
                    skill.start();
                    return true;
                }
            } else if (skill instanceof AMElectronCurtains && skill.canSpell()) {
                validDist = ((AMElectronCurtains) skill).getMaxDistance();
                if (validDist * validDist >= curDist) {
                    skill.start();
                    return true;
                }
            } else if (skill instanceof AMLocationTeleport && skill.canSpell()) {
                validDist = ((AMLocationTeleport) skill).getMaxDistance();
                if (validDist * validDist >= curDist) {
                    skill.start();
                    return true;
                }
            } else if (skill instanceof AMThunderBolt && skill.canSpell()) {
                validDist = ((AMThunderBolt) skill).getMaxDistance();
                if (validDist * validDist >= curDist) if (isTargetInHorizon(owner, target)) {
                    skill.start();
                    return true;
                }
            } else if (skill instanceof AMElectronMissile && skill.canSpell()) {
                validDist = ((AMElectronMissile) skill).getMaxDistance();
                if (validDist * validDist >= curDist) {
                    skill.start();
                    return true;
                }
            } else if (skill instanceof AMRailgun && skill.canSpell()) {
                validDist = ((AMRailgun) skill).getMaxDistance();
                if (validDist * validDist >= curDist) if (isTargetInHorizon(owner, target)) {
                    skill.start();
                    return true;
                }
            } else if (skill instanceof AMLocManip && skill.canSpell()) {
                validDist = 7;
                if (validDist * validDist >= curDist) {
                    skill.start();
                    return true;
                }
            } else if (skill instanceof AMThunderClap && skill.canSpell()) {
                validDist = ((AMThunderClap) skill).getMaxDistance();
                if (validDist * validDist >= curDist && ((validDist - 1) * (validDist - 1) <= curDist))
                    if (isTargetInHorizon(owner, target)) {
                        skill.start();
                        return true;
                    }
            } else if (skill instanceof AMPlasmaCannon && skill.canSpell()) {
                skill.start();
                return true;
            }
        }
        return true;
    }
}
