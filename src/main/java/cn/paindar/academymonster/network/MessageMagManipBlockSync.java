package cn.paindar.academymonster.network;

import cn.lambdalib2.util.SideUtils;
import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.entity.EntityMagManipBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Paindar on 2017/6/5.
 */
public class MessageMagManipBlockSync implements IMessage, IMsgAction
{
    @Override
    public boolean execute() {

        World world = SideUtils.getWorld(nbt.getInteger("world"));
        EntityMagManipBlock mob = (EntityMagManipBlock) world.getEntityByID(nbt.getInteger("id"));
        boolean value = nbt.getBoolean("value");
        if (mob==null)
        {
            AcademyMonster.log.warn("<ArcGen>Fail to find entity whose id is "+nbt.getInteger("id"));
            return true;
        }
        mob.setPlaceWhenCollide(value);

        return true;
    }

    public static class Handler implements IMessageHandler<MessageMagManipBlockSync, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageMagManipBlockSync msg, MessageContext ctx)
        {
            if (ctx.side == Side.CLIENT)
            {
                NetworkManager.addAction(msg);
            }
            return null;
        }
    }
    NBTTagCompound nbt;
    public MessageMagManipBlockSync(){}
    public MessageMagManipBlockSync(EntityMagManipBlock entity, boolean value)
    {
        nbt = new NBTTagCompound();
        nbt.setInteger("world", entity.dimension);
        nbt.setInteger("id",entity.getEntityId());
        nbt.setBoolean("value",value);
    }


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
