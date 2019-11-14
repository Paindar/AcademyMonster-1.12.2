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
public class MessageArcGenEffect extends MessageAutos11n
{
    @Override
    public boolean execute() {
        if (speller==null)
        {
            AcademyMonster.log.warn("<ArcGen>Fail to find entity.");
            return true;
        }
        EntityMobArc arc = new EntityMobArc(speller, ArcPatterns.weakArc);
        arc.texWiggle = 0.7;
        arc.showWiggle = 0.1;
        arc.hideWiggle = 0.4;
        arc.addMotionHandler(new Life(10));
        arc.lengthFixed = false;
        arc.length = range;

        speller.world.spawnEntity(arc);
        speller.world.playSound(speller.posX, speller.posY, speller.posZ,
                new SoundEvent(new ResourceLocation("academy","em.arc_weak")), SoundCategory.HOSTILE
                ,.5f,1f,false);
        return true;
    }

    public static class H extends Handler<MessageArcGenEffect>
    {

    }

    EntityLivingBase speller;
    float range;

    public MessageArcGenEffect(){}

    public MessageArcGenEffect(EntityLivingBase speller, float range)
    {
        this.speller = speller;
        this.range = range;
    }
}
