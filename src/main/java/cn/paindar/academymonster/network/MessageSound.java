package cn.paindar.academymonster.network;

import cn.academy.client.sound.ACSounds;
import cn.lambdalib2.util.SideUtils;
import cn.paindar.academymonster.core.AcademyMonster;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

/**
 * Created by Paindar on 2017/2/9.
 */
public class MessageSound extends MessageAutos11n
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

    public static class H extends Handler<MessageSound> {}
    String sound;
    int worldId;
    int entityId;
    float vol;

    public MessageSound() {}

   public MessageSound(String sound, EntityLivingBase target,float vol)
    {
        this.sound = sound;
        this.worldId = target.dimension;
        this.entityId = target.getEntityId();
        this.vol = vol;
    }
}

