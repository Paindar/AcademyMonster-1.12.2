package cn.paindar.academymonster.ability;

import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.lambdalib2.s11n.network.TargetPoints;
import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.Raytrace;
import cn.lambdalib2.util.WorldUtils;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.ability.client.EffectSpawner;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.entity.EntityMobMDRay;
import cn.paindar.academymonster.entity.EntityMobMdBall;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import cn.paindar.academymonster.network.NetworkManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.lambdalib2.util.MathUtils.lerp;

public class AMElectronMissile extends SkillTemplate
{
    public static final AMElectronMissile Instance = new AMElectronMissile();
    protected AMElectronMissile()
    {
        super("electron_missile");
    }
    @Override
    public MonsterSkillInstance create(Entity e) {
        return new ElectronMissileContext(e);
    }

    @Override
    public SpellingInfo fromBytes(ByteBuf buf) {
        SpellingInfo info = new ElectronMissileClientInfo();
        info.fromBytes(buf);
        return info;
    }
    static class ElectronMissileContext extends MonsterSkillInstance
    {
        private final List<EntityMobMdBall> ballList=new ArrayList<>();
        private final int freq;
        private final int maxTick;
        private final float range;
        private final double damage;
        private int time;
        private final int cooldown;
        public ElectronMissileContext(Entity e)
        {
            super(Instance, e);
            freq=(int)lerp(20,10,getExp());
            maxTick=(int)lerp(100,200,getExp());
            range=(int)lerp(8,12,getExp());
            damage=lerp(5,12,getExp());
            cooldown = (int)lerp(800,400,getExp());
        }

        @Override
        public int execute() {
            time=0;
            if(speller instanceof EntityLivingBase)
            {
                ((EntityLivingBase) speller).addPotionEffect(new PotionEffect(
                        Objects.requireNonNull(Potion.getPotionFromResourceLocation("speed")), maxTick, 2));
            }
            return WAITING;
        }
        @Override
        public void tick()
        {
            time++;
            if(speller.isDead)
            {
                clear();
                return;
            }
            if(time%freq==0)
            {
                EntityMobMdBall ball=new EntityMobMdBall(speller,2333333);
                speller.world.spawnEntity(ball);
                ballList.add(ball);

            }

            if( !ballList.isEmpty())
            {
                List<Entity> list= WorldUtils.getEntities(speller,range, EntitySelectors.exclude(speller).
                        and(EntitySelectors.living()).
                        and((Entity e)-> !(e instanceof EntityPlayer && ((EntityPlayer) e).capabilities.isCreativeMode)));

                if(!list.isEmpty())
                {
                    Entity ball = ballList.get(0);
                    for(Entity targ : list)
                    {
                        Vec3d str= ball.getPositionEyes(1f);
                        Vec3d dst= targ.getPositionEyes(1f);
                        RayTraceResult trace = Raytrace.perform(speller.world, str,dst
                                , EntitySelectors.exclude(speller).and(EntitySelectors.living()));
                        if (trace.typeOfHit == RayTraceResult.Type.ENTITY)
                        {
                            ballList.remove(0);
                            attack((EntityLivingBase) trace.entityHit,damage,false);
                            ElectronMissileClientInfo info = new ElectronMissileClientInfo();
                            info.str = str;
                            info.end = dst;
                            NetworkManager.sendSkillEventAllAround(TargetPoints.convert(speller, 19),speller, Instance, info);
                            ball.setDead();
                            break;
                        }
//                        else if (trace.typeOfHit == RayTraceResult.Type.BLOCK)
//                        {
//                            IBlockState ibs = speller.world.getBlockState(trace.getBlockPos());
//
//                        }
                    }
                }
            }
            if (time>=maxTick)
                clear();
        }

        @Override
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
    static class ElectronMissileClientInfo extends SpellingInfo
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
