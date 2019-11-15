/**
 * Copyright (c) Lambda Innovation, 2013-2016
 * This file is part of the AcademyCraft mod.
 * https://github.com/LambdaInnovation/AcademyCraft
 * Licensed under GPLv3, see project root for more information.
 */
package cn.paindar.academymonster.playerskill.meltdowner;

import cn.academy.ability.Skill;
import cn.academy.ability.context.ClientRuntime;
import cn.academy.ability.context.Context;
import cn.academy.client.render.particle.MdParticleFactory;
import cn.academy.client.render.util.ACRenderingHelper;
import cn.academy.client.sound.ACSounds;
import cn.academy.client.sound.FollowEntitySound;
import cn.academy.entity.EntityMDRay;
import cn.academy.event.BlockDestroyEvent;
import cn.lambdalib2.s11n.network.NetworkMessage.Listener;
import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.MathUtils;
import cn.lambdalib2.util.RandUtils;
import cn.lambdalib2.util.WorldUtils;
import cn.paindar.academymonster.entity.EntityMobMDRay;
import cn.paindar.academymonster.events.RayShootingEvent;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static cn.lambdalib2.util.MathUtils.lerpf;
import static cn.lambdalib2.util.RandUtils.ranged;
import static cn.lambdalib2.util.RandUtils.rangei;
import static cn.lambdalib2.util.VecUtils.*;

public class Meltdowner extends Skill {

    public static final Meltdowner instance = new Meltdowner();

    private Meltdowner() {
        super("meltdowner", 3);
    }

    @Override
    public void activate(ClientRuntime rt, int keyid) {
        activateSingleKey2(rt, keyid, MDContext::new);
    }

    public static class MDContext extends Context {

        private static final String
                MSG_PERFORM = "perform";

        int ticks;

        static final int TICKS_MIN = 20, TICKS_MAX = 40, TICKS_TOLE = 100;
        static final float STEP = 0.5f;

        final float exp = ctx.getSkillExp(), range=2;
        final float tickConsumption = lerpf(15, 27, exp);

        @SideOnly(Side.CLIENT)
        FollowEntitySound sound;

        public MDContext(EntityPlayer player) {
            super(player, instance);
        }

        @Listener(channel=Context.MSG_KEYUP, side=Side.CLIENT)
        void l_keyUp() {
            if (ticks >= TICKS_MIN) {
                sendToServer(MSG_PERFORM);
            } else {
                terminate();
            }
        }

        @Listener(channel=Context.MSG_KEYABORT, side=Side.CLIENT)
        void l_keyAbort() {
            terminate();
        }

        @Listener(channel=MSG_TICK, side={Side.CLIENT, Side.SERVER})
        void g_tick() {
            ++ticks;

            if (!isRemote()) {
                if (!ctx.consume(0, tickConsumption) || ticks > TICKS_TOLE) {
                    terminate();
                }
            }
        }

        @SideOnly(Side.CLIENT)
        @Listener(channel=MSG_MADEALIVE, side=Side.CLIENT)
        void c_start() {
            sound = new FollowEntitySound(player, "md.md_charge", SoundCategory.PLAYERS).setVolume(1.0f);
            ACSounds.playClient(sound);
        }

        @SideOnly(Side.CLIENT)
        @Listener(channel=MSG_TICK, side=Side.CLIENT)
        void c_tick() {
            if(isLocal()) {
                player.capabilities.setPlayerWalkSpeed(0.1f - ticks * 0.001f);
            }

            // Particles surrounding player
            int count = rangei(2, 3);
            while(count --> 0) {
                double r = ranged(0.7, 1);
                double theta = ranged(0, Math.PI * 2);
                double h = ranged(-1.2, 0);
                Vec3d pos = add(new Vec3d(player.posX,
                        player.posY + (ACRenderingHelper.isThePlayer(player) ? 0 : 1.6), player.posZ),
                        new Vec3d(r * Math.sin(theta), h, r * Math.cos(theta)));
                Vec3d vel = new Vec3d(ranged(-.03, .03), ranged(.01, .05), ranged(-.03, .03));
                world().spawnEntity(MdParticleFactory.INSTANCE.next(world(), pos, vel));
            }
        }

        @SideOnly(Side.CLIENT)
        @Listener(channel=MSG_TERMINATED, side=Side.CLIENT)
        void c_terminate() {
            if (isLocal()) {
                player.capabilities.setPlayerWalkSpeed(0.1f);
            }
            sound.stop();
        }

        private List<Entity> selectTargets(EntityLivingBase entity, EntityPlayer player, double incr_)
        {
            Vec2f py = player.getPitchYaw();
            float yaw = -py.y*3.1415926f/180.0f - MathUtils.PI_F * 0.5f,
                    pitch = py.x*3.1415926f/180.0f;
            Vec3d start = entity.getPositionEyes(1f);
            Vec3d slope = player.getLookVec().rotatePitch(RandUtils.rangef(-.5f, .5f)*0.1f).rotateYaw(RandUtils.rangef(-.5f, .5f)*0.1f);

            Vec3d vp0 = new Vec3d(0, 0, 1);
            vp0 = vp0.rotatePitch(pitch).rotateYaw(yaw);

            Vec3d vp1 = new Vec3d(0, 1, 0);
            vp1.rotatePitch(pitch);
            vp1.rotateYaw(yaw);
            Vec3d v0 = add(start, add(multiply(vp0, -range), multiply(vp1, -range))),
                    v1 = add(start, add(multiply(vp0, range), multiply(vp1, -range))),
                    v2 = add(start, add(multiply(vp0, range), multiply(vp1, range))),
                    v3 = add(start, add(multiply(vp0, -range), multiply(vp1, range))),
                    v4 = add(v0, multiply(slope, incr_)),
                    v5 = add(v1, multiply(slope, incr_)),
                    v6 = add(v2, multiply(slope, incr_)),
                    v7 = add(v3, multiply(slope, incr_));
            AxisAlignedBB aabb = WorldUtils.minimumBounds(v0, v1, v2, v3, v4, v5, v6, v7);

            Predicate<Entity> areaSelector = target -> {
                Vec3d dv = subtract(new Vec3d(target.posX, target.posY, target.posZ), start);
                Vec3d proj = dv.crossProduct(slope);
                return !target.equals(entity) && proj.length() < range * 1.2;
            };
            return WorldUtils.getEntities(player.world, aabb, EntitySelectors.everything().and(areaSelector));

        }
        @Listener(channel=MSG_PERFORM, side=Side.SERVER)
        void s_perform() {
            final float exp = ctx.getSkillExp();

            float cp     = lerpf(340, 455, exp);
            float overload = lerpf(300, 200, exp);
            if (ctx.consume(overload, 0)) {
                int ct = toChargeTicks();
                float energy = getEnergy(ct);
                float dmg = getDamage(ct);

                EntityLivingBase lastEntity=player;
                World world=lastEntity.world;
                final double maxIncrement=30;
                double incr_=maxIncrement;

        /* Apply Entity Damage */
                {
                    boolean reflectCheck = true;
                    List<Vec3d> paths = new ArrayList<>();
                    Vec3d pos=new Vec3d(lastEntity.posX, lastEntity.posY +lastEntity.getEyeHeight(),
                            lastEntity.posZ);
                    paths.add(pos);
                    paths.add(add(pos, multiply(lastEntity.getLookVec(), incr_)));
                    while (reflectCheck) {
                        reflectCheck=false;
                        if(incr_<=0)
                            break;
                        List<Entity> targets = selectTargets(lastEntity, player, incr_);
                        targets.sort(Comparator.comparingDouble(lastEntity::getDistanceSq));
                        for (Entity e : targets) {
                            if (e instanceof EntityLivingBase) {
                                RayShootingEvent event = new RayShootingEvent(player, (EntityLivingBase) e, incr_);
                                boolean result = MinecraftForge.EVENT_BUS.post(event);
                                incr_=event.range;
                                if (!result)
                                    ctx.attack(e, dmg);
                                else {
                                    incr_ -= (e.getDistanceSq(lastEntity));
                                    paths.remove(paths.size()-1);
                                    pos=new Vec3d(e.posX, e.posY +e.getEyeHeight(), e.posZ);
                                    paths.add(pos);
                                    paths.add(add(pos,multiply(e.getLookVec(), incr_)));
                                    lastEntity = (EntityLivingBase) e;
                                    reflectCheck=true;
                                    break;
                                }
                            } else {
                                e.setDead();
                            }
                        }
                    }

                    {
                        int index=0;
                        Vec3d str=paths.get(index), end=paths.get(index+1), dir=subtract(end, str);
                        float yaw = (float)(-MathUtils.PI_F * 0.5f -Math.atan2(dir.x, dir.z)),
                                pitch = (float) -Math.atan2(dir.y,
                                        Math.sqrt(dir.x * dir.x + dir.z * dir.z));
                        Vec3d vp0 = new Vec3d(0, 0, 1).rotatePitch(pitch).rotateYaw(yaw);

                        Vec3d vp1 = new Vec3d(0, 1, 0).rotatePitch(pitch).rotateYaw(yaw);
                        incr_=1;
                        for(int i=1;i<=maxIncrement;i++)
                        {
                            if(incr_>=dir.length()||i==maxIncrement || energy<=0)
                            {
                                index++;
                                if(energy<=0)
                                {
                                    end=add(str, multiply(dir.normalize(),incr_));
                                }
                                incr_=1;
                                if(!player.world.isRemote)
                                {
                                    sendToClient(MSG_PERFORM, str,end);
                                }
                                if(index==paths.size()-1)
                                    break;
                                str=paths.get(index);
                                end=paths.get(index+1);
                                dir=subtract(end, str);
                                yaw = (float)(-MathUtils.PI_F * 0.5f -Math.atan2(dir.x, dir.z));
                                pitch = (float) -Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z));
                                vp0 = new Vec3d(0, 0, 1).rotatePitch(pitch).rotateYaw(yaw);

                                vp1 = new Vec3d(0, 1, 0).rotatePitch(pitch).rotateYaw(yaw);
                            }
                            if(ctx.canBreakBlock(player.world)) {
                                Vec3d cur=add(str,multiply(dir.normalize(),incr_));
                                for (double s = -range; s <= range; s += STEP) {
                                    for (double t = -range; t <= range; t += STEP) {
                                        double rr = range * RandUtils.ranged(0.9, 1.1);
                                        if (s * s + t * t > rr * rr)
                                            continue;
                                        pos = add(cur, add(multiply(vp0, s), multiply(vp1, t)));
                                        energy = destroyBlock(world, (int) pos.x, (int) pos.y,
                                                (int) pos.z, energy);
                                    }
                                }
                            }

                            incr_++;
                        }
                    }

                }

                ctx.addSkillExp(getExpIncr(ct));
                ctx.setCooldown(getCooldown(ct));
                terminate();
            }
        }

        private float destroyBlock(World world, int x, int y, int z, float energy) {
            BlockPos pos = new BlockPos(x, y, z);
            IBlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();
            float hardness = blockState.getBlockHardness(world, pos);
            if(hardness < 0)
                hardness = 233333;
            if(!MinecraftForge.EVENT_BUS.post(new BlockDestroyEvent(world, pos)) && energy >= hardness) {
                if(blockState.getMaterial() != Material.AIR) {
                    block.dropBlockAsItemWithChance(world, pos, blockState, 0.05f, 0);

                    if(RandUtils.ranged(0, 1) < 0.1) {
                        SoundType st = block.getSoundType(blockState, world, pos, null);
                        SoundEvent breakSnd = st.getBreakSound();
                        world.playSound(
                                pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                                breakSnd,
                                SoundCategory.BLOCKS,
                                (st.getVolume() + 1.0F) / 2.0F,
                                st.getPitch(), false);
                    }
                }
                world.setBlockToAir(pos);
                return energy - hardness;
            }
            return 0;
        }


        @SideOnly(Side.CLIENT)
        @Listener(channel=MSG_PERFORM, side=Side.CLIENT)
        void c_perform(Vec3d str, Vec3d end) {
            player.world.spawnEntity(new EntityMDRay(player, end.subtract(str).length()));
        }

        private float timeRate(int ct) {
            return MathUtils.lerpf(0.8f, 1.2f, (ct - 20.0f) / 20.0f);
        }

        private float getEnergy(int ct) {
            return timeRate(ct) * MathUtils.lerpf(300, 700, exp);
        }

        private float getDamage(int ct) {
            return timeRate(ct) * MathUtils.lerpf(20, 50, exp);
        }

        private int getCooldown(int ct) {
            return (int)(timeRate(ct) * 20 * MathUtils.lerpf(15, 7, exp));
        }

        private float getExpIncr(int ct) {
            return timeRate(ct) * 0.002f;
        }

        private int toChargeTicks() {
            return Math.min(ticks, TICKS_MAX);
        }
    }

}