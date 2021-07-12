package cn.paindar.academymonster.ability;

import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.lambdalib2.s11n.network.TargetPoints;
import cn.lambdalib2.util.*;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.ability.client.EffectSpawner;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import cn.paindar.academymonster.entity.EntityMobMDRay;
import cn.paindar.academymonster.entity.EntityMobMdBall;
import cn.paindar.academymonster.entity.EntityRailgunFXNative;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import cn.paindar.academymonster.network.NetworkManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

import static cn.lambdalib2.util.MathUtils.lerp;

public class AMScatterBomb extends SkillTemplate{
    public static final AMScatterBomb Instance = new AMScatterBomb();
    protected AMScatterBomb()
    {
        super("scatter_bomb");
    }
    @Override
    public MonsterSkillInstance create(Entity e) {
        return new ScatterBombContext(e);
    }

    @Override
    public SpellingInfo fromBytes(ByteBuf buf) {
        ScatterClientInfo info = new ScatterClientInfo();
        info.fromBytes(buf);
        return info;
    }

    static class ScatterBombContext extends MonsterSkillInstance
    {
        private static final float MAX_TIME = 120;
        private List<EntityMobMdBall> ballList=new ArrayList<>();
        private final double damage;
        private final double range;
        private final int cooldown;
        private float charging;

        public ScatterBombContext(Entity ent) {
            super(Instance, ent);
            damage=lerp(3,9,getExp());
            range=lerp(5,11,getExp());
            cooldown = (int)lerp(160,240,getExp());
        }

        @Override
        public int execute() {
            charging=0;
            return WAITING;
        }

        @Override
        public void tick() {
            charging++;
            if(charging>20&&charging%10==0 && charging<=100)
            {
                EntityMobMdBall ball=new EntityMobMdBall(speller,2333333);
                ballList.add(ball);
                speller.world.spawnEntity(ball);
            }
            else if(charging>=MAX_TIME)
            {
                stop();
            }
            else if (speller.isDead)
            {
                clear();
            }
        }
        public void stop()
        {
            List<Entity> trgList= WorldUtils.getEntities(speller,range, EntitySelectors.living().and(EntitySelectors.exclude(speller)));
            Vec3d dst= VecUtils.lookingPos(speller, range);
            ScatterClientInfo info = new ScatterClientInfo();
            for(EntityMobMdBall ball:ballList)
            {
                Vec3d str=ball.getPositionVector();
                if(!trgList.isEmpty())
                {
                    if(getExp()>0.6)
                    {
                        dst = trgList.get(RandUtils.nextInt(trgList.size())).getPositionVector();
                    }
                    else
                        dst = dst.rotatePitch(MathUtils.toRadians((RandUtils.nextFloat() - 0.5F) * 25))
                                .rotateYaw(MathUtils.toRadians((RandUtils.nextFloat() - 0.5F) * 25));
                }
                RayTraceResult trace = Raytrace.perform(speller.world,str,dst
                        , EntitySelectors.exclude(speller).and(EntitySelectors.living()));
                if (trace.typeOfHit == RayTraceResult.Type.ENTITY && trace.entityHit != null)
                {
                    attack((EntityLivingBase) trace.entityHit, damage,false);
                }
                info.traces.add(str);
                info.traces.add(dst);
                ball.setDead();
            }
            NetworkManager.sendSkillEventAllAround(TargetPoints.convert(speller, 15), speller,
                    Instance, info);
            clear();
        }

        public void clear() {

            setDisposed();
            MobSkillData.get((EntityMob) speller).getSkillData().setCooldown(template, cooldown);
            for(EntityMobMdBall ball:ballList)
            {
                ball.setDead();
            }
            ballList.clear();
        }
    }
    static class ScatterClientInfo extends SpellingInfo
    {
        List<Vec3d> traces = new ArrayList<>();
        @Override
        @SideOnly(Side.CLIENT)
        public void action(Entity speller) {
            for(int idx=0;idx<traces.size();idx+=2)
            {
                EntityMobMDRay raySmall = new EntityMobMDRay(speller, traces.get(idx), traces.get(idx+1));
                raySmall.viewOptimize = false;
                //speller.world.spawnEntity(raySmall);
                EffectSpawner.Instance.addEffect(raySmall);
            }
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            traces = NetworkS11n.deserialize(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            NetworkS11n.serialize(buf, traces, false);
        }
    }
}
