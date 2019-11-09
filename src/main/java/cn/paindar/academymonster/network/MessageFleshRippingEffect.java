package cn.paindar.academymonster.network;

import cn.academy.client.render.util.ACRenderingHelper;
import cn.academy.client.sound.ACSounds;
import cn.academy.entity.EntityBloodSplash;
import cn.lambdalib2.s11n.nbt.NBTS11n;
import cn.lambdalib2.util.RandUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sun.nio.ch.Net;

/**
 * Created by Paindar on 2017/2/11.
 */
public class MessageFleshRippingEffect implements IMessage, IMsgAction
{
    @Override
    public boolean execute() {
        ACSounds.playClient(target, "tp.guts", SoundCategory.HOSTILE, 0.6f);
        for(int i = 0; i< RandUtils.rangei(4, 6); i++)
        {
            double y = target.posY + RandUtils.ranged(0, 1) * target.height;
            if(target instanceof EntityPlayer)
                y += ACRenderingHelper.getHeightFix((EntityPlayer)target);

            double theta = RandUtils.ranged(0, Math.PI * 2);
            double r  = 0.5 * RandUtils.ranged(0.8 * target.width, target.width);
            EntityBloodSplash splash = new EntityBloodSplash(target.world);
            splash.setPosition(target.posX + r * Math.sin(theta), y, target.posZ + r * Math.cos(theta));
            target.world.spawnEntity(splash);
        }
        return true;
    }

    public static class Handler implements IMessageHandler<MessageFleshRippingEffect, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageFleshRippingEffect msg, MessageContext ctx)
        {
            EntityLivingBase target = msg.target;

            if (ctx.side == Side.CLIENT && target!=null)
            {
                NetworkManager.addAction(msg);
            }
            return null;
        }
    }
    EntityLivingBase target;

    public MessageFleshRippingEffect(){}

    public MessageFleshRippingEffect(EntityLivingBase target)
    {
        this.target = target;
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
