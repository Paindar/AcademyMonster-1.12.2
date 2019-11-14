package cn.paindar.academymonster.network;

import cn.paindar.academymonster.entity.datapart.MobSkillData;
import net.minecraft.entity.monster.EntityMob;


/**
 * Created by Paindar on 2017/2/15.
 */
public class MessageSkillInfoSync extends MessageAutos11n
{
    @Override
    public boolean execute() {
        if(entity != null)
        {
            MobSkillData info= MobSkillData.get(entity);
            info.setSkillData(list);
        }
        return true;
    }

    public static class H extends Handler<MessageSkillInfoSync> { }

    String list="";
    EntityMob entity;

    public MessageSkillInfoSync(){}

    public MessageSkillInfoSync(EntityMob entity)
    {
        list = MobSkillData.get(entity).getSkillData();
        this.entity = entity;
    }
}
