package cn.paindar.academymonster.network;

import cn.paindar.academymonster.ability.AMThunderBolt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paindar on 2017/3/7.
 */
public class MessageThunderBolt extends MessageAutos11n
{
    @Override
    public boolean execute() {
        AMThunderBolt.spawnEffect(source, target, aoes);
        return true;
    }
    public static class H extends Handler<MessageThunderBolt>{}

    EntityMob source;
    Vec3d target;
    List<Entity> aoes = new ArrayList<>();

    public MessageThunderBolt(){}

    public MessageThunderBolt(EntityMob source, Vec3d target, List<Entity> aoes)
    {
        this.source = source;
        this.target = target;
        this.aoes.addAll(aoes);
    }

}
