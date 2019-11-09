package cn.paindar.academymonster.network;

import cn.lambdalib2.s11n.nbt.NBTS11n;
import cn.paindar.academymonster.ability.AMThunderBolt;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paindar on 2017/3/7.
 */
public class MessageThunderBolt implements IMessage, IMsgAction
{
    @Override
    public boolean execute() {
        Vec3d target = new Vec3d(tx, ty, tz);
        AMThunderBolt.spawnEffect(source, target, aoes);
        return true;
    }

    public static class Handler implements IMessageHandler<MessageThunderBolt, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageThunderBolt msg, MessageContext ctx)
        {
            NetworkManager.addAction(msg);
            return null;
        }
    }

    EntityMob source;
    double tx, ty, tz;
    List<Entity> aoes = new ArrayList<>();

    public MessageThunderBolt(){}

    public MessageThunderBolt(EntityMob source, Vec3d target, List<Entity> aoes)
    {
        this.source = source;
        this.tx = target.x; this.ty = target.y; this.tz = target.z;
        this.aoes.addAll(aoes);
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
