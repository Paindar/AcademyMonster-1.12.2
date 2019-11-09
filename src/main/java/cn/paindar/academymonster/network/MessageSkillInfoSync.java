package cn.paindar.academymonster.network;

import cn.lambdalib2.s11n.nbt.NBTS11n;
import cn.lambdalib2.util.SideUtils;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * Created by Paindar on 2017/2/15.
 */
public class MessageSkillInfoSync implements IMessage
{
    public static class Handler implements IMessageHandler<MessageSkillInfoSync, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageSkillInfoSync msg, MessageContext ctx)
        {
            if (ctx.side == Side.CLIENT)
            {
                World world = SideUtils.getWorld(msg.world);
                EntityMob entity= (EntityMob) world.getEntityByID(msg.id);
                if(entity != null)
                {
                    MobSkillData info= MobSkillData.get(entity);
                    info.setSkillData(msg.list);
                }
            }
            return null;
        }
    }

    String list="";
    int world = 0;
    int id = 0;

    public MessageSkillInfoSync(){}

    public MessageSkillInfoSync(EntityMob entity)
    {
        list = MobSkillData.get(entity).getSkillData();
        world = entity.dimension;
        id = entity.getEntityId();
    }
    /**
     * Convert from the supplied buffer into your specific message type
     *
     * @param buf
     */

    @Override
    public void fromBytes(ByteBuf buf)
    {
        NBTTagCompound nbt= ByteBufUtils.readTag(buf);
        NBTS11n.read(nbt, this);

    }

    /**
     * Deconstruct your message into the supplied byte buffer
     *
     * @param buf
     */
    @Override
    public void toBytes(ByteBuf buf)
    {
        NBTTagCompound nbt=new NBTTagCompound();
        NBTS11n.write(nbt, this);
        ByteBufUtils.writeTag(buf, nbt);
    }
}
