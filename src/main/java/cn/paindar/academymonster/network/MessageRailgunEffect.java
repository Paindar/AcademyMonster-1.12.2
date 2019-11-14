package cn.paindar.academymonster.network;

import cn.paindar.academymonster.entity.EntityRailgunFXNative;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.Vec3d;

/**
 * Created by Paindar on 2017/2/12.
 */
public class MessageRailgunEffect extends MessageAutos11n
{
    @Override
    public boolean execute() {
        mob.world.spawnEntity(new EntityRailgunFXNative(mob, str, end));
        return true;
    }

    public static class H extends Handler<MessageRailgunEffect> {}

    EntityMob mob;
    public Vec3d str, end;

    public MessageRailgunEffect(){}

    public MessageRailgunEffect(EntityMob speller, Vec3d str, Vec3d end)
    {
        this.mob = speller;
        this.str = str;
        this.end = end;
    }
}
