package cn.paindar.academymonster.network;

import cn.academy.client.sound.ACSounds;
import cn.lambdalib2.s11n.nbt.NBTS11n;
import cn.lambdalib2.util.SideUtils;
import cn.paindar.academymonster.core.AcademyMonster;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Paindar on 2017/2/9.
 */
public class MessageSound  implements IMessage, IMsgAction
{
    @Override
    public boolean execute() {
        World world = SideUtils.getWorld(worldId);
        Entity entity = world.getEntityByID(entityId);
        if(sound==null) {
            AcademyMonster.log.warn("<Penerate Teleport>Failed to find sound.");
            return true;
        }
        ACSounds.playClient(entity, sound, SoundCategory.HOSTILE, vol);
        return true;
    }

    public static class Handler implements IMessageHandler<MessageSound, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageSound msg, MessageContext ctx)
        {
            if (ctx.side == Side.CLIENT) {
                NetworkManager.addAction(msg);
            }
            return null;
        }
    }
    String sound;
    int worldId;
    int entityId;
    float vol, pitch;

    public MessageSound() { }

   public MessageSound(String sound, EntityLivingBase target,float vol)
    {
        this.sound = sound;
        this.worldId = target.dimension;
        this.entityId = target.getEntityId();
        this.vol = vol;
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

