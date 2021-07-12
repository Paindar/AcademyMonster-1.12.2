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

public class AMElectronBomb extends SkillTemplate
{
    public static final AMElectronBomb Instance = new AMElectronBomb();
    protected AMElectronBomb() {
        super("electron_bomb");
    }

    @Override
    public MonsterSkillInstance create(Entity e) {
        return new ElectronBombContext(e);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpellingInfo fromBytes(ByteBuf buf) {
        SpellingInfo info = new ElectronBombClientInfo();
        info.fromBytes(buf);
        return info;
    }
    static class ElectronBombContext extends MonsterSkillInstance
    {
        private final double maxDistance;
        private final double damage;
        private final int cooldown;
        public ElectronBombContext(Entity ent) {
            super(Instance, ent);

            damage=lerp(6, 12, getExp());
            maxDistance=lerp(7,10,getExp());
            cooldown = (int)lerp(55,25, getExp());
        }

        private Vec3d getDest(Entity speller){return Raytrace.getLookingPos(speller, maxDistance, null, null).getLeft();}

        @Override
        public int execute() {
            EntityMobMdBall ball = new EntityMobMdBall(speller,(int)lerp(30,15,getExp()), ball1 ->
            {
                Vec3d str= ball1.getPositionEyes(1f),
                        end=getDest(speller);
                RayTraceResult trace = Raytrace.perform(speller.getEntityWorld(),str,end
                        , EntitySelectors.exclude(speller).and(EntitySelectors.living()));
                if (trace.typeOfHit == RayTraceResult.Type.ENTITY && trace.entityHit != null)
                {
                    attack((EntityLivingBase) trace.entityHit, damage, false);
                }
                ElectronBombClientInfo info = new ElectronBombClientInfo();
                info.str = str;
                info.end = end;
                NetworkManager.sendSkillEventAllAround(TargetPoints.convert(speller, 25), speller,
                        Instance, info);
            }) ;
            speller.world.spawnEntity(ball);
            setDisposed();
            return cooldown;
        }

        @Override
        public void clear() {

        }
    }
    static class ElectronBombClientInfo extends SpellingInfo
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
