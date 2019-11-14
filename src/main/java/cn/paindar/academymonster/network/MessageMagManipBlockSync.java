package cn.paindar.academymonster.network;

import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.entity.EntityMagManipBlock;

/**
 * Created by Paindar on 2017/6/5.
 */
public class MessageMagManipBlockSync extends MessageAutos11n
{
    @Override
    public boolean execute() {

        if (entity==null)
        {
            AcademyMonster.log.warn("<ArcGen>Fail to find entity.");
            return true;
        }
        entity.setPlaceWhenCollide(value);

        return true;
    }

    public static class H extends Handler<MessageMagManipBlockSync> { }
    EntityMagManipBlock entity;
    boolean value;
    public MessageMagManipBlockSync(){}
    public MessageMagManipBlockSync(EntityMagManipBlock entity, boolean value)
    {
        this.entity = entity;
        this.value = value;
    }

}
