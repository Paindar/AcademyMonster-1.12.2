package cn.paindar.academymonster.network;

import cn.lambdalib2.s11n.network.NetworkS11n;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;

public abstract class MessageAutos11n  implements IMessage, IMsgAction {
    @Override
    public abstract boolean execute();

    protected static class Handler<T extends MessageAutos11n> implements IMessageHandler<T, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(T msg, MessageContext ctx)
        {
            NetworkManager.addAction(msg);
            return null;
        }
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        try {
            for(Field field : this.getClass().getDeclaredFields())
            {
                field.set(this, NetworkS11n.deserialize(buf));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                Object obj = field.get(this);
                NetworkS11n.serialize(buf, obj, false);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
