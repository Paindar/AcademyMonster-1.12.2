package cn.paindar.academymonster.ability;

import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.WorldUtils;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;

import java.util.List;

import static cn.lambdalib2.util.MathUtils.lerp;

public class AMPenetrateTeleport extends SkillTemplate
{
    public static final AMPenetrateTeleport Instance = new AMPenetrateTeleport();
    protected AMPenetrateTeleport()
    {
        super("penetrate_teleport");
    }
    public double getMaxDistance(double exp){return lerp(3,10, exp);}
    @Override
    public MonsterSkillInstance create(Entity e) {
        return new PenetrateTeleportContext(e);
    }

    @Override
    public SpellingInfo fromBytes(ByteBuf buf) {
        return null;
    }
    public static class PenetrateTeleportContext extends MonsterSkillInstance
    {
        private final double maxDistance;
        private final int cooldown;
        public PenetrateTeleportContext(Entity e)
        {
            super(Instance, e);
            maxDistance=lerp(3,10, getExp());
            cooldown = (int)lerp(200, 100, getExp());
        }

        public void startTeleport(double x, double y, double z)
        {
            if(speller.isRiding())
                speller.dismountRidingEntity();
            speller.setPositionAndUpdate(x,y,z);
            speller.fallDistance = 0;
            clear();
        }
        @Override
        public void tick()
        {
            if(speller.isDead)
                clear();
        }

        @Override
        public void clear()
        {
            setDisposed();
            MobSkillData.get((EntityMob) speller).getSkillData().setCooldown(template, cooldown);

        }
        @Override
        public int execute() {
            return WAITING;
        }
    }
}
