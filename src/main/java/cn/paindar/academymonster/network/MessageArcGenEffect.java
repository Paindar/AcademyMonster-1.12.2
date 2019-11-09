package cn.paindar.academymonster.network;

import cn.academy.client.render.util.ArcPatterns;
import cn.academy.client.sound.ACSounds;
import cn.lambdalib2.s11n.nbt.NBTS11n;
import cn.lambdalib2.util.SideUtils;
import cn.lambdalib2.util.entityx.handlers.Life;
import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.entity.EntityMobArc;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Paindar on 2017/2/17.
 */
public class MessageArcGenEffect implements IMessage, IMsgAction
{
    @Override
    public boolean execute() {
        World world = Minecraft.getMinecraft().world;
        //World world= SideUtils.getWorld(msg.nbt.getInteger("world"));
        EntityLivingBase speller= (EntityLivingBase) world.getEntityByID(nbt.getInteger("mob"));
        if (speller==null)
        {
            AcademyMonster.log.warn("<ArcGen>Fail to find entity whose id is "+nbt.getInteger("mob"));
            return true;
        }
        EntityMobArc arc = new EntityMobArc(speller, ArcPatterns.weakArc);
        arc.texWiggle = 0.7;
        arc.showWiggle = 0.1;
        arc.hideWiggle = 0.4;
        arc.addMotionHandler(new Life(10));
        arc.lengthFixed = false;
        arc.length = nbt.getDouble("range");
        arc.setWorld(world);

        world.spawnEntity(arc);
        world.playSound(speller.posX, speller.posY, speller.posZ,
                new SoundEvent(new ResourceLocation("academy","em.arc_weak")), SoundCategory.HOSTILE
                ,.5f,1f,false);
        return true;
    }

    public static class Handler implements IMessageHandler<MessageArcGenEffect, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageArcGenEffect msg, MessageContext ctx)
        {
            if (ctx.side == Side.CLIENT)
            {
                NetworkManager.addAction(msg);
            }
            return null;
        }
    }

    NBTTagCompound nbt;
    EntityLivingBase mob;
    float range;

    public MessageArcGenEffect(){}

    public MessageArcGenEffect(EntityLivingBase speller, float range)
    {
        nbt = new NBTTagCompound();
        nbt.setInteger("world", speller.dimension);
        nbt.setInteger("mob", speller.getEntityId());
        nbt.setDouble("range", range);
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
