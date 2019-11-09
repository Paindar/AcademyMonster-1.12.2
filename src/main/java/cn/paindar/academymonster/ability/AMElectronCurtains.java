package cn.paindar.academymonster.ability;

import cn.lambdalib2.s11n.network.NetworkMessage;
import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.Raytrace;
import cn.lambdalib2.util.WorldUtils;
import cn.paindar.academymonster.entity.EntityMobMDRay;
import cn.paindar.academymonster.entity.EntityMobMdBall;
import cn.paindar.academymonster.network.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

import static cn.lambdalib2.util.MathUtils.lerpf;

/**
 * Created by Paindar on 2017/6/4.
 */
public class AMElectronCurtains extends BaseSkill
{
    private double maxDamage;
    private double maxDistance;
    private int maxAmounts;
    public AMElectronCurtains(EntityMob speller, float exp)
    {
        super(speller, (int)lerpf(40,120,exp), exp, "meltdowner.electron_curtain");
        maxDamage=lerpf(6,12,exp);
        maxDistance=lerpf(7,15,exp);
        maxAmounts=(int)lerpf(8,20,exp);
    }
    public double getMaxDistance(){return maxDistance;}

    @Override
    public void start()
    {
        super.start();
        if(canSpell())
        {
            double part=2.0*Math.PI/maxAmounts;
            for(int i=0;i<maxAmounts;i++)
            {
                EntityMobMdBall ball = new EntityMobMdBall(speller,(int)lerpf(20,5,getSkillExp()),
                        ball1 ->{
                    Vec3d str= ball1.getPositionVector(),
                            end=new Vec3d(ball1.posZ+(ball1.posX-speller.posX)*getMaxDistance()/1.23,
                                    0,
                                    ball1.posZ+(ball1.posZ-speller.posZ)*getMaxDistance()/1.23);
                    RayTraceResult trace = Raytrace.perform(speller.world,str,end
                            , EntitySelectors.exclude(speller).and(EntitySelectors.living()));
                    if (trace.typeOfHit== RayTraceResult.Type.ENTITY)
                    {
                        attack((EntityLivingBase) trace.entityHit,(float)maxDamage);
                    }
                    List<Entity> list= WorldUtils.getEntities(speller,25,EntitySelectors.player());
                    for(Entity e:list)
                    {
                        NetworkManager.sendMdRayEffectTo(str,end, speller, (EntityPlayerMP) e);
                    }
                }) ;
                ball.setSubPos((float)Math.cos(part*i)*1.23f,(float)Math.sin(part*i)*1.23f);
                speller.world.spawnEntity(ball);
            }
            cooldown();
        }
    }

}
