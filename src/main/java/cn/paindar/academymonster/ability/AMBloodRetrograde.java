package cn.paindar.academymonster.ability;

import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.Raytrace;
import cn.lambdalib2.util.WorldUtils;
import cn.paindar.academymonster.network.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.RayTraceResult;

import java.util.List;

import static cn.lambdalib2.util.MathUtils.lerpf;

/**
 * Created by voidcl on 2017/3/16.
 */
public class AMBloodRetrograde extends BaseSkill {

    private float damage;
    private float exp;
    private float range;
    public AMBloodRetrograde(EntityMob speller, float exp)
    {
        super(speller,(int)lerpf(60,30,exp),exp,"vecmanip.blood_retro");
        this.exp=exp;
        damage=lerpf(7,14,exp);
        range=lerpf(1,4,exp);
    }

    @Override
    public void start()
    {
        super.start();
        RayTraceResult result= Raytrace.traceLiving(speller,range, EntitySelectors.living());
        EntityLivingBase target=null;
        if(result.typeOfHit==RayTraceResult.Type.ENTITY)
        {
            target=(EntityLivingBase)result.entityHit;
        }
        if(target!=null&&!isSkillInCooldown())
        {
            attackIgnoreArmor(target,damage);
            target.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("poison"), (int)(200*getSkillExp()), 2));
            List<Entity> list= WorldUtils.getEntities(speller,25,EntitySelectors.player());
            for(Entity e:list)
            {
                NetworkManager.sendFleshRippingEffectTo(target,(EntityPlayerMP)e);
            }
        }else
        {
            return ;
        }
        cooldown();
    }

    public float getMaxDistance(){return range;}

}
