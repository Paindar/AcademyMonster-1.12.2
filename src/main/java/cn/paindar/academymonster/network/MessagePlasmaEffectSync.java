package cn.paindar.academymonster.network;

import cn.paindar.academymonster.entity.EntityPlasmaBodyEffect;
import cn.paindar.academymonster.entity.EntityTornadoEffect;
import net.minecraft.entity.Entity;

/**
 * Created by Paindar on 2017/6/7.
 */
public class MessagePlasmaEffectSync extends MessageAutos11n
{
    @Override
    public boolean execute() {
        if(entity!=null)
            if(entity instanceof EntityPlasmaBodyEffect)
                ((EntityPlasmaBodyEffect)entity).changeState();
            else if(entity instanceof EntityTornadoEffect)
                ((EntityTornadoEffect)entity).changeState();
        return true;
    }

    public static class H extends Handler<MessagePlasmaEffectSync> { }
    Entity entity;

    public MessagePlasmaEffectSync(){}

    public MessagePlasmaEffectSync(Entity speller)
    {
        entity = speller;
    }

}
