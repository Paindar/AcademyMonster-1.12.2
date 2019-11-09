package cn.paindar.academymonster.ability;

import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.VecUtils;
import cn.lambdalib2.util.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static cn.lambdalib2.util.MathUtils.lerpf;


/**
 * Created by Paindar on 2017/2/23.
 */
public class AMThunderClap extends BaseSkill
{
    private float damage;
    private float range;
    private float distance=15;
    public AMThunderClap(EntityMob speller, float exp)
    {
        super(speller,(int) lerpf(600,300,exp), exp,"electromaster.thunder_clap");
        damage=lerpf(18,60,exp);
        range=lerpf(5,15,exp);
    }

    @Override
    public void start()
    {
        super.start();
        Vec3d target = VecUtils.lookingPos(speller, 14);
        EntityLightningBolt lightning = new EntityLightningBolt(speller.world, target.x, target.y, target.z, true);
        speller.world.addWeatherEffect(lightning);
        List<Entity> list= WorldUtils.getEntities(speller,range, EntitySelectors.exclude(speller).and(EntitySelectors.living()));
        for(Entity e:list)
        {
            attack((EntityLivingBase)e,damage);
        }
        cooldown();
    }

    public float getMaxDistance(){return distance;}
}
