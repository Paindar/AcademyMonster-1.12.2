package cn.paindar.academymonster.ability;

import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.lambdalib2.s11n.network.TargetPoints;
import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.Raytrace;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.ability.client.EffectSpawner;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import cn.paindar.academymonster.entity.EntityMobMDRay;
import cn.paindar.academymonster.entity.EntityMobMdBall;
import cn.paindar.academymonster.network.NetworkManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static cn.lambdalib2.util.MathUtils.lerp;

public class AMElectronCurtains extends SkillTemplate{
    public static final AMElectronCurtains Instance = new AMElectronCurtains();
    protected AMElectronCurtains()
    {
        super("electron_curtains");
    }
    @Override
    public MonsterSkillInstance create(Entity e) {
        return new ElectronCurtainsContext(e);
    }

    @Override
    public SpellingInfo fromBytes(ByteBuf buf) {
        ElectronCurtainsClientInfo info = new ElectronCurtainsClientInfo();
        info.fromBytes(buf);
        return info;
    }
    static class ElectronCurtainsContext extends MonsterSkillInstance
    {
        private final double maxDamage;
        private final double maxDistance;
        private final int maxAmounts;
        private final int cooldown;
        public ElectronCurtainsContext(Entity ent) {
            super(Instance, ent);
            maxDamage=lerp(6,12,getExp());
            maxDistance=lerp(7,15,getExp());
            maxAmounts=(int)lerp(8,20,getExp());
            cooldown = (int)lerp(40,120,getExp());
        }
        public double getMaxDistance(){return maxDistance;}

        @Override
        public int execute() {
            double part=2.0*Math.PI/maxAmounts;
            for(int i=0;i<maxAmounts;i++)
            {
                EntityMobMdBall ball = new EntityMobMdBall(speller,(int)lerp(20,5,getExp()),
                        ball1 ->{
                            Vec3d str= ball1.getPositionVector(),
                                    end=new Vec3d(ball1.posZ+(ball1.posX-speller.posX)*getMaxDistance()/1.23,
                                            0,
                                            ball1.posZ+(ball1.posZ-speller.posZ)*getMaxDistance()/1.23);
                            RayTraceResult trace = Raytrace.perform(speller.world,str,end
                                    , EntitySelectors.exclude(speller).and(EntitySelectors.living()));
                            if (trace.typeOfHit== RayTraceResult.Type.ENTITY)
                            {
                                attack((EntityLivingBase) trace.entityHit,(float)maxDamage,false);
                            }
                            ElectronCurtainsClientInfo info = new ElectronCurtainsClientInfo();
                            info.str = str;
                            info.end = end;
                            NetworkManager.sendSkillEventAllAround(TargetPoints.convert(speller, 15), speller,
                                    Instance, info);

                        }) ;
                ball.setSubPos((float)Math.cos(part*i)*1.23f,(float)Math.sin(part*i)*1.23f);
                speller.world.spawnEntity(ball);
            }
            setDisposed();
            return cooldown;
        }

        @Override
        public void clear() {

        }
    }
    static class ElectronCurtainsClientInfo extends SpellingInfo
    {
        Vec3d str, end;
        @Override
        @SideOnly(Side.CLIENT)
        public void action(Entity speller) {
            EntityMobMDRay raySmall  = new EntityMobMDRay(speller, str, end);
            raySmall.viewOptimize = false;
            //speller.world.spawnEntity(raySmall);
            EffectSpawner.Instance.addEffect(raySmall);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            str = NetworkS11n.deserialize(buf);
            end = NetworkS11n.deserialize(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            NetworkS11n.serialize(buf, str, false);
            NetworkS11n.serialize(buf, end, false);
        }
    }
}
