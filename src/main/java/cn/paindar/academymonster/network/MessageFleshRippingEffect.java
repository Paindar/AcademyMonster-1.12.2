package cn.paindar.academymonster.network;

import cn.academy.client.render.util.ACRenderingHelper;
import cn.academy.client.sound.ACSounds;
import cn.academy.entity.EntityBloodSplash;
import cn.lambdalib2.util.RandUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;

/**
 * Created by Paindar on 2017/2/11.
 */
public class MessageFleshRippingEffect extends MessageAutos11n
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

    public static class H extends Handler<MessageFleshRippingEffect> { }
    EntityLivingBase target;

    public MessageFleshRippingEffect(){}

    public MessageFleshRippingEffect(EntityLivingBase target)
    {
        this.target = target;
    }
}
