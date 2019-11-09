package cn.paindar.academymonster.network;

import cn.lambdalib2.util.SideUtils;
import cn.paindar.academymonster.entity.EntityPlasmaBodyEffect;
import cn.paindar.academymonster.entity.EntityTornadoEffect;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Paindar on 2017/6/7.
 */
public class MessagePlasmaEffectSync implements IMessage, IMsgAction
{
    @Override
    public boolean execute() {
        World world = SideUtils.getWorld(nbt.getInteger("world"));
        Entity entity=  world.getEntityByID(nbt.getInteger("i"));
        if(entity!=null)
            if(entity instanceof EntityPlasmaBodyEffect)
                ((EntityPlasmaBodyEffect)entity).changeState();
            else if(entity instanceof EntityTornadoEffect)
                ((EntityTornadoEffect)entity).changeState();
        return true;
    }

    public static class Handler implements IMessageHandler<MessagePlasmaEffectSync, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessagePlasmaEffectSync message, MessageContext ctx)
        {
            if (ctx.side == Side.CLIENT)
            {
                NetworkManager.addAction(message);
            }
            return null;
        }
    }
    NBTTagCompound nbt;

    public MessagePlasmaEffectSync(){}

    public MessagePlasmaEffectSync(Entity speller)
    {
        nbt=new NBTTagCompound();
        nbt.setInteger("world", speller.dimension);
        nbt.setInteger("i",speller.getEntityId());
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        nbt= ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, nbt);
    }
}
