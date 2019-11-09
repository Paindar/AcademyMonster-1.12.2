package cn.paindar.academymonster.network;

import cn.lambdalib2.s11n.nbt.NBTS11n;
import cn.paindar.academymonster.entity.EntityRailgunFXNative;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Paindar on 2017/2/12.
 */
public class MessageRailgunEffect implements IMessage, IMsgAction
{
    @Override
    public boolean execute() {
        Vec3d str = new Vec3d(sx, sy, sz),
                end = new Vec3d(ex, ey, ez);
        mob.world.spawnEntity(new EntityRailgunFXNative(mob, str, end));
        return true;
    }

    public static class Handler implements IMessageHandler<MessageRailgunEffect, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageRailgunEffect msg, MessageContext ctx)
        {
            if (ctx.side == Side.CLIENT)
            {
                NetworkManager.addAction(msg);
            }
            return null;
        }
    }

    EntityMob mob;
    public double sx, sy, sz,
            ex, ey, ez;

    public MessageRailgunEffect(){}

    public MessageRailgunEffect(EntityMob speller, Vec3d str, Vec3d end)
    {
        this.mob = speller;
        sx = str.x;sy = str.y; sz = str.z;
        ex = end.x;ey = end.y; ez = end.z;
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
