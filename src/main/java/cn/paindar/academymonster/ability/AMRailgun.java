package cn.paindar.academymonster.ability;

import cn.academy.client.sound.ACSounds;
import cn.academy.event.BlockDestroyEvent;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.lambdalib2.s11n.network.TargetPoints;
import cn.lambdalib2.util.*;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.ability.client.EffectSpawner;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import cn.paindar.academymonster.config.AMConfig;
import cn.paindar.academymonster.entity.EntityMobCoinThrowing;
import cn.paindar.academymonster.entity.EntityRailgunFXNative;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import cn.paindar.academymonster.events.RayShootingEvent;
import cn.paindar.academymonster.network.NetworkManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static cn.lambdalib2.util.MathUtils.lerp;
import static cn.lambdalib2.util.VecUtils.*;

public class AMRailgun extends SkillTemplate {
    public static final AMRailgun Instance = new AMRailgun();
    public static boolean canDestroyBlock = true;

    @StateEventCallback
    public static void postInit(FMLPostInitializationEvent evt)
    {
        canDestroyBlock = AMConfig.getBoolean("am.skill.loc_manip.destroyBlock",true);
    }

    protected AMRailgun() {
        super("railgun");
    }

    @Override
    public MonsterSkillInstance create(Entity e) {
        return new AMRailgun.RailgunContext(e);
    }

    @Override
    public SpellingInfo fromBytes(ByteBuf buf) {
        RailgunClientInfo ret = new RailgunClientInfo();
        ret.fromBytes(buf);
        return ret;
    }

    static class RailgunContext extends MonsterSkillInstance
    {
        private static final double STEP = 0.5;
        private final double damage;
        private final double range =2;
        private final int maxIncrement;
        private final int cooldown;
        private double energy;
        private EntityMobCoinThrowing coin;
        public RailgunContext(Entity ent) {
            super(Instance, ent);
            maxIncrement=(int)lerp(12,25,getExp());
            damage=lerp(20, 50, getExp());
            energy = lerp(900, 2000, getExp());
            cooldown = (int)lerp(800, 600, getExp());
        }

        private void destroyBlock(World world, int x, int y, int z) {
            BlockPos pos = new BlockPos(x, y, z);
            IBlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();
            float hardness = blockState.getBlockHardness(world, pos);
            if(hardness < 0)
                hardness = 233333;
            if(hardness<= energy &&!MinecraftForge.EVENT_BUS.post(new BlockDestroyEvent(world, pos)))
            {
                if(blockState.getMaterial() != Material.AIR) {
                    block.dropBlockAsItemWithChance(world, pos, blockState, 0.05f, 0);
                    world.setBlockToAir(pos);
                    energy -=hardness;
                }
            }
            else
                energy = 0;
        }

        private List<Entity> selectTargets(Entity entity, double incr_)
        {
            Vec2f py = entity.getPitchYaw();
            float yaw = -py.y*3.1415926f/180.0f - MathUtils.PI_F * 0.5f,
                    pitch = py.x*3.1415926f/180.0f;
            Vec3d start = entity.getPositionEyes(1f);
            Vec3d slope = entity.getLookVec().rotatePitch(RandUtils.rangef(-.5f, .5f)*0.1f).rotateYaw(RandUtils.rangef(-.5f, .5f)*0.1f);

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
            return WorldUtils.getEntities(speller.world, aabb, EntitySelectors.everything().and(areaSelector));

        }
        @Override
        public void tick()
        {
            if(coin==null || coin.isDead||speller.isDead)
            {
                setCooldown();
                return;
            }
            if(coin.getProgress()>0.9)
            {
                coin.setDead();
                perform();
                setCooldown();
            }
        }

        @Override
        public void clear() {
            setCooldown();
        }

        private void perform()
        {
            Entity lastEntity=speller;
            World world=speller.world;
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
                    List<Entity> targets = selectTargets(lastEntity, incr_);
                    targets.sort(Comparator.comparingDouble(lastEntity::getDistanceSq));
                    for (Entity e : targets) {
                        if (e instanceof EntityLivingBase) {
                            RayShootingEvent event = new RayShootingEvent(speller, (EntityLivingBase) e, incr_);
                            boolean result = MinecraftForge.EVENT_BUS.post(event);
                            incr_=event.range;
                            if (!result)
                                attack((EntityLivingBase) e, damage, false);
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
                            pitch = (float) -Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z));
                    Vec3d vp0 = new Vec3d(0, 0, 1).rotatePitch(pitch).rotateYaw(yaw);

                    Vec3d vp1 = new Vec3d(0, 1, 0).rotatePitch(pitch).rotateYaw(yaw);
                    incr_=1;
                    for(int i=1;i<=maxIncrement;i++)
                    {
                        if(incr_>=dir.length()||i==maxIncrement)
                        {
                            incr_=1;
                            index++;
                            RailgunClientInfo info = new RailgunClientInfo();
                            info.stage = RailgunClientInfo.Stage.FIRE;
                            info.str = str; info.end = end;
                            NetworkManager.sendSkillEventAllAround(TargetPoints.convert(speller, 25)
                                    ,speller, Instance, info);

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
                        if(canDestroyBlock) {
                            Vec3d cur=add(str,multiply(dir.normalize(),incr_));
                            for (double s = -range; s <= range; s += STEP) {
                                for (double t = -range; t <= range; t += STEP) {
                                    double rr = range * RandUtils.ranged(0.9, 1.1);
                                    if (s * s + t * t > rr * rr)
                                        continue;
                                    pos = VecUtils.add(cur,
                                            VecUtils.add(
                                                    VecUtils.multiply(vp0, s),
                                                    VecUtils.multiply(vp1, t)));
                                    destroyBlock(world, (int) pos.x, (int) pos.y, (int) pos.z);
                                }
                            }
                        }
                        incr_++;
                    }
                }

            }

        }

        @Override
        public int execute()
        {
            coin=new EntityMobCoinThrowing(speller);
            speller.world.spawnEntity(coin);
            RailgunClientInfo info = new RailgunClientInfo();
            info.stage = RailgunClientInfo.Stage.THROW;
            NetworkManager.sendSkillEventAllAround(TargetPoints.convert(speller, 15)
                    ,speller, Instance, info);

            return WAITING;
        }
        private void setCooldown()
        {
            MobSkillData.get((EntityMob) speller).getSkillData().setCooldown(template, cooldown);
            setDisposed();
        }
    }
    static class RailgunClientInfo extends SpellingInfo
    {
        enum Stage{THROW, FIRE}
        public Stage stage;
        Vec3d str, end;
        @Override
        @SideOnly(Side.CLIENT)
        public void action(Entity speller) {
            switch(stage)
            {
                case THROW:
                    ACSounds.playClient(speller, "entity.flipcoin", SoundCategory.HOSTILE,.5F);
                    break;
                case FIRE:
                    //speller.world.spawnEntity(new EntityRailgunFXNative(speller, str, end));
                    EffectSpawner.Instance.addEffect(new EntityRailgunFXNative(speller, str, end));
            }
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            int enum_id = NetworkS11n.deserialize(buf);
            stage = Stage.values()[enum_id];
            if(stage == Stage.FIRE)
            {
                str = NetworkS11n.deserialize(buf);
                end = NetworkS11n.deserialize(buf);
            }
        }

        @Override
        public void toBytes(ByteBuf buf) {
            NetworkS11n.serialize(buf,stage.ordinal(),false );
            if(stage == Stage.FIRE)
            {
                NetworkS11n.serialize(buf,str,false);
                NetworkS11n.serialize(buf,end,false);
            }
        }
    }
}
