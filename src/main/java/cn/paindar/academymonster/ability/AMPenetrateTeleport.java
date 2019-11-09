package cn.paindar.academymonster.ability;

import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.WorldUtils;
import cn.paindar.academymonster.network.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;

import static cn.lambdalib2.util.MathUtils.lerpf;

/**
 * Created by Paindar on 2017/2/9.
 */
public class AMPenetrateTeleport extends BaseSkill
{
    private float maxDistance;
    public AMPenetrateTeleport(EntityMob speller, float exp) {
        super(speller,(int)lerpf(200, 100, exp), exp,"teleporter.penetrate_teleport");
        maxDistance=lerpf(3,10, getSkillExp());
    }

    public float getMaxDistance(){return maxDistance;}


    public void startTeleport(double x, double y, double z)
    {
        start();
        if(speller.isRiding())
            speller.dismountRidingEntity();
        speller.setPositionAndUpdate(x,y,z);
        speller.fallDistance = 0;
        if(!speller.world.isRemote)
        {
            List<Entity> list= WorldUtils.getEntities(speller, 25, EntitySelectors.player());
            for(Entity e:list)
            {

                NetworkManager.sendSoundTo("tp.tp",speller,.5f,(EntityPlayerMP)e);
            }
        }
        cooldown();
    }


}
