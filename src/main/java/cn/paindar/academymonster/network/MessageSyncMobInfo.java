package cn.paindar.academymonster.network;

import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.core.SkillManager;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import cn.paindar.academymonster.entity.datapart.MonsterSkillList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class MessageSyncMobInfo implements IMessage {
    public static class Handler implements IMessageHandler<MessageSyncMobInfo, IMessage>
    {
        @Override
        public IMessage onMessage(MessageSyncMobInfo msg, MessageContext ctx)
        {
            MobSkillData data;
            switch (msg.stage)
            {
                case REQ:
                    ByteBuf buf = Unpooled.buffer(512);
                    data = MobSkillData.get(msg.e);
                    NetworkS11n.serialize(buf, data, false);
                    MessageSyncMobInfo msg_ = new MessageSyncMobInfo(msg.e, data.getSkillData(), data.level, data.catalog.ordinal());
                    NetworkManager.sendToClient(ctx.getServerHandler().player, msg_);
                    break;
                case RESP:
                    data = MobSkillData.get(msg.e);
                    data.getSkillData().fromBytes(msg.buf);
                    data.level = msg.level;
                    data.catalog = SkillManager.Catalog.values()[msg.cat_id];
            }
            return null;
        }

    }
    private enum Stage{REQ,RESP}
    public Stage stage;
    public EntityMob e;
    public ByteBuf buf;
    public int level;
    public int cat_id;
    public MessageSyncMobInfo(){
        buf = Unpooled.buffer(512);
    }
    public MessageSyncMobInfo(EntityMob e, MonsterSkillList list, int level, int cat_id)
    {
        this.e = e;
        if(e.world.isRemote)
        {
            stage=Stage.REQ;
        }
        else
        {
            buf = Unpooled.buffer(512);
            stage=Stage.RESP;
            list.toBytes(buf);
            this.level = level;
            this.cat_id = cat_id;
        }
    }
    @Override
    public void fromBytes(ByteBuf buf) {
        int stg_id = NetworkS11n.deserialize(buf);
        stage = Stage.values()[stg_id];
        e = NetworkS11n.deserialize(buf);
        switch(stage)
        {
            case REQ:
                break;
            case RESP:
                level = NetworkS11n.deserialize(buf);
                cat_id = NetworkS11n.deserialize(buf);
                this.buf.writeBytes(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkS11n.serialize(buf, stage.ordinal(), false);
        NetworkS11n.serialize(buf, e, false);
        switch(stage)
        {
            case REQ:
                break;
            case RESP:
                NetworkS11n.serialize(buf, level, false);
                NetworkS11n.serialize(buf, cat_id, true);
                buf.writeBytes(this.buf);
        }

    }
}
