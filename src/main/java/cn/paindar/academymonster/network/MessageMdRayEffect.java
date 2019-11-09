package cn.paindar.academymonster.network;

import cn.lambdalib2.s11n.nbt.NBTS11n;
import cn.lambdalib2.util.SideUtils;
import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.entity.EntityMobMDRay;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sun.nio.ch.Net;

/**
 * Created by Paindar on 2017/2/10.
 */
public class MessageMdRayEffect implements IMessage, IMsgAction
{
    @Override
    public boolean execute() {
        Vec3d str = new Vec3d(nbt.getDouble("sx"), nbt.getDouble("sy"), nbt.getDouble("sz")),
                end = new Vec3d(nbt.getDouble("ex"), nbt.getDouble("ey"), nbt.getDouble("ez"));
        World world = SideUtils.getWorld(nbt.getInteger("world"));
        EntityMob mob = (EntityMob) world.getEntityByID(nbt.getInteger("id"));
        if (mob==null)
        {
            AcademyMonster.log.warn("<ArcGen>Fail to find entity whose id is "+nbt.getInteger("id"));
            return true;
        }
        EntityMobMDRay raySmall  = new EntityMobMDRay(mob, str, end);
        raySmall.viewOptimize = false;
        world.spawnEntity(raySmall);
        return true;
    }

    public static class Handler implements IMessageHandler<MessageMdRayEffect, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageMdRayEffect msg, MessageContext ctx)
        {
            if (ctx.side == Side.CLIENT)
            {
                NetworkManager.addAction(msg);
            }
            return null;
        }
    }
    public NBTTagCompound nbt;

    public MessageMdRayEffect(){}

    public MessageMdRayEffect(Vec3d str, Vec3d end, EntityMob spawner) {
        nbt = new NBTTagCompound();
        nbt.setDouble("sx", str.x);
        nbt.setDouble("sy", str.y);
        nbt.setDouble("sz", str.z);
        nbt.setDouble("ex", end.x);
        nbt.setDouble("ey", end.y);
        nbt.setDouble("ez", end.z);
        nbt.setInteger("world", spawner.dimension);
        nbt.setInteger("id",spawner.getEntityId());
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
