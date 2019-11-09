package cn.paindar.academymonster.ability;

import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.s11n.network.TargetPoints;
import cn.lambdalib2.util.BlockSelectors;
import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.Raytrace;
import cn.paindar.academymonster.config.AMConfig;
import cn.paindar.academymonster.entity.EntityPlasmaBodyEffect;
import cn.paindar.academymonster.entity.EntityTornadoEffect;
import cn.paindar.academymonster.network.NetworkManager;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import static cn.lambdalib2.util.MathUtils.lerpf;

public class AMPlasmaCannon extends BaseSkill {
    public static boolean canDestroyBlock = true;
    @StateEventCallback
    public static void postInit(FMLPostInitializationEvent evt)
    {
        canDestroyBlock = AMConfig.getBoolean("am.skill.PlasmaCannon.destroyBlock", true);
    }
    public double time;
    public EntityPlasmaBodyEffect effect;
    public EntityTornadoEffect body;

    public AMPlasmaCannon(EntityMob speller, float exp) {
        super(speller, (int) lerpf(400,650,exp),exp,"vecmanip.plasma_cannon");
    }

    @Override
    public void start()
    {
        super.start();
        effect = new EntityPlasmaBodyEffect(speller, this);
        time=0;
        effect.setPosition(speller.posX, speller.posY + 15, speller.posZ);
        body=new EntityTornadoEffect(speller.world, speller);
        speller.world.spawnEntity(body);
        speller.world.spawnEntity(effect);
    }

    @Override
    public void onTick()
    {
        super.onTick();
        if(!isActivated())
            return;
        time++;
        if(time>60)
        {
            cooldown();
            Vec3d result = Raytrace.getLookingPos(speller, 10, EntitySelectors.nothing(), BlockSelectors.filEverything).getKey();
            flyTo(result.x, result.y, result.z);
        }
        else if(effect==null||effect.isDead) {
            clear();
            cooldown();
        }
    }

    private void flyTo(double x, double y, double z)
    {
        effect.setTargetPoint(x,y,z);
        NetworkManager.sendPlasmaStateChange(TargetPoints.convert(effect, 20),body);
    }

    @Override
    public void clear() {
        super.clear();
        if(effect != null)
            NetworkManager.sendPlasmaStateChange(TargetPoints.convert(effect, 20),effect);
    }
}
