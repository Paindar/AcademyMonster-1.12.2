package cn.paindar.academymonster.network;

import cn.lambdalib2.s11n.nbt.NBTS11n;
import cn.paindar.academymonster.entity.EntityMagManipBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Paindar on 2017/6/5.
 */
public class MessageMagManipBlockSync implements IMessage
{
    public static class Handler implements IMessageHandler<MessageMagManipBlockSync, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageMagManipBlockSync msg, MessageContext ctx)
        {
            if (ctx.side == Side.CLIENT)
            {
                msg.entity.setPlaceWhenCollide(msg.value);
            }
            return null;
        }
    }
    EntityMagManipBlock entity;
    boolean value;
    public MessageMagManipBlockSync(){}
    public MessageMagManipBlockSync(EntityMagManipBlock entity, boolean value)
    {
        this.entity = entity;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        NBTTagCompound nbt= ByteBufUtils.readTag(buf);
        NBTS11n.read(nbt, this);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        NBTTagCompound nbt=new NBTTagCompound();
        NBTS11n.write(nbt, this);
        ByteBufUtils.writeTag(buf, nbt);
    }
}
