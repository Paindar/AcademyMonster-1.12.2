package cn.paindar.academymonster.network;

import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.paindar.academymonster.ability.SkillTemplate;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.core.SkillManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Paindar on 2017/2/17.
 */
public class MessageSkillEvent implements IMessage
{
    public static class Handler implements IMessageHandler<MessageSkillEvent, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageSkillEvent msg, MessageContext ctx)
        {
            SpellingInfo info = msg.info;
            if(info!=null && msg.speller!=null)
                info.action(msg.speller);
            return null;
        }

    }

    Entity speller;
    SkillTemplate skill;
    SpellingInfo info;

    public MessageSkillEvent(Entity speller, SkillTemplate skill, SpellingInfo info)
    {
        this.speller = speller;
        this.skill = skill;
        this.info = info;
    }

    @Deprecated
    public MessageSkillEvent() { }


    @Override
    public void fromBytes(ByteBuf buf) {
        try
        {
            speller = NetworkS11n.deserialize(buf);
        }
        catch(Exception e)
        {
            AcademyMonster.log.info("fail to deserialize package, maybe client is loading.");
            speller = null;
        }
        String skillId = NetworkS11n.deserialize(buf);
        skill = SkillManager.instance.getSkillTemplate(skillId);
        info = skill.fromBytes(buf);

    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkS11n.serialize(buf, speller, false);
        NetworkS11n.serialize(buf, skill.getId(), false);
        if(info!=null)
            info.toBytes(buf);
    }
}
