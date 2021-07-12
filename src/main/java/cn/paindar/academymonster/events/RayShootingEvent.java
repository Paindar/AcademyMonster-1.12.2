package cn.paindar.academymonster.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Post by AIRailgun and AIMeltdowner.
 * This event will be post when any entity is going to attack by one of these skill.
 * This event is cancelable, which means this entity reflect attack.
 * Its range is how far skill can extend.
 */
public class RayShootingEvent extends Event {
    public final Entity source;
    public final EntityLivingBase target;
    public double range;
    public RayShootingEvent(Entity source, EntityLivingBase target, double range){
        this.source=source;
        this.target=target;
        this.range=range;
    }

    @Override
    public boolean isCancelable()
    {
        return true;
    }
}
