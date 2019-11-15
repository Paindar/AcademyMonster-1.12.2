package cn.paindar.academymonster.playerskill.electromaster;

import cn.academy.ability.AbilityContext;
import cn.academy.ability.Skill;
import cn.academy.ability.context.ClientRuntime;
import cn.academy.ability.context.DelegateState;
import cn.academy.ability.context.KeyDelegate;
import cn.academy.client.render.misc.RailgunHandEffect;
import cn.academy.datapart.CPData;
import cn.academy.datapart.PresetData;
import cn.academy.entity.EntityCoinThrowing;
import cn.academy.event.BlockDestroyEvent;
import cn.academy.event.CoinThrowEvent;
import cn.lambdalib2.renderhook.DummyRenderData;
import cn.lambdalib2.s11n.network.NetworkMessage;
import cn.lambdalib2.s11n.network.NetworkMessage.Listener;
import cn.lambdalib2.s11n.network.TargetPoints;
import cn.lambdalib2.util.*;

import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.entity.EntityRailgunFXNative;
import cn.paindar.academymonster.events.RayShootingEvent;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.function.Predicate;

import static cn.lambdalib2.util.MathUtils.lerpf;
import static cn.lambdalib2.util.VecUtils.*;

public class Railgun extends Skill {

    public static final Railgun instance = new Railgun();

    private static final String
            MSG_CHARGE_EFFECT = "charge_eff",
            MSG_PERFORM       = "perform",
            MSG_REFLECT       = "reflect",
            MSG_COIN_PERFORM  = "coin_perform",
            MSG_ITEM_PERFORM  = "item_perform";

    private static final double
            REFLECT_DISTANCE = 15;
    private static final float range = 2;
    private static final float STEP=.5F;

    private Set<Item> acceptedItems = new HashSet<>();
    {
        acceptedItems.add(Items.IRON_INGOT);
        acceptedItems.add(Item.getItemFromBlock(Blocks.IRON_BLOCK));
    }

    private Railgun() {
        super("railgun", 4);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private boolean isAccepted(ItemStack stack) {
        return stack != null && acceptedItems.contains(stack.getItem());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void activate(ClientRuntime rt, int keyID) {
        rt.addKey(keyID, new Delegate());
    }

    @SubscribeEvent
    public void onThrowCoin(CoinThrowEvent evt) {
        CPData cpData = CPData.get(evt.getEntityPlayer());
        PresetData pData = PresetData.get(evt.getEntityPlayer());

        boolean spawn = cpData.canUseAbility() &&
                pData.getCurrentPreset().hasControllable(this);

        if (spawn) {
            if (SideUtils.isClient()) {
                spawnClientEffect(evt.getEntityPlayer());

                informDelegate(evt.coin);
            } else {
                NetworkMessage.sendToAllAround(
                        TargetPoints.convert(evt.getEntityPlayer(), 30),
                        instance,
                        MSG_CHARGE_EFFECT,
                        evt.getEntityPlayer()
                );
            }
        }
    }

    private void informDelegate(EntityCoinThrowing coin) {
        ClientRuntime rt = ClientRuntime.instance();
        Collection<KeyDelegate> delegates = rt.getDelegates(ClientRuntime.DEFAULT_GROUP);
        if (!delegates.isEmpty()) {
            for (Iterator<KeyDelegate> i = delegates.iterator(); i.hasNext(); ) {
                KeyDelegate dele = i.next();
                if (dele instanceof Delegate) {
                    ((Delegate) dele).informThrowCoin(coin);
                    return;
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Listener(channel=MSG_CHARGE_EFFECT, side= Side.CLIENT)
    private void hSpawnClientEffect(EntityPlayer target) {
        spawnClientEffect(target);
    }

    @SideOnly(Side.CLIENT)
    private void spawnClientEffect(EntityPlayer target) {
        DummyRenderData.get(target).addRenderHook(new RailgunHandEffect());
    }

    @SideOnly(Side.CLIENT)
    @Listener(channel=MSG_PERFORM, side=Side.CLIENT)
    public void performClient(EntityPlayer player, Vec3d str, Vec3d end) {
        player.world.spawnEntity(new EntityRailgunFXNative(player, str, end));
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

    private void performServer(EntityPlayer player) {
        AbilityContext ctx = AbilityContext.of(player, this);

        final float exp = ctx.getSkillExp();

        float cp     = lerpf(340, 455, exp);
        float overload = lerpf(160, 110, exp);
        if (ctx.consume(overload, cp)) {
            float dmg = lerpf(40, 100, exp);
            float energy = lerpf(900, 2000, exp);

            EntityLivingBase lastEntity = player;
            World world=lastEntity.world;
            final double maxIncrement=45;
            double incr_=maxIncrement;

        /* Apply Entity Damage */
            {
                boolean reflectCheck = true;
                List<Vec3d> paths = new ArrayList<>();
                Vec3d pos=new Vec3d(lastEntity.posX, lastEntity.posY +lastEntity.getEyeHeight(), lastEntity.posZ);
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
                            AcademyMonster.log.info("try attack "+e.getName());
                            if (!result) {
                                ctx.attack(e, dmg);
                                AcademyMonster.log.info("attack "+e.getName()+", damage = "+dmg);
                            }
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
                                NetworkMessage.sendToAllAround(
                                        TargetPoints.convert(player, 30),
                                        instance,
                                        MSG_PERFORM,
                                        player,
                                        str,end
                                );
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
            ctx.addSkillExp(0.005f);
            ctx.setCooldown((int) lerpf(300, 160, exp));
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


    @Listener(channel=MSG_COIN_PERFORM, side=Side.SERVER)
    private void consumeCoinAtServer(EntityPlayer player, EntityCoinThrowing coin) {
        coin.setDead();
        performServer(player);
    }

    @Listener(channel=MSG_ITEM_PERFORM, side=Side.SERVER)
    private void consumeItemAtServer(EntityPlayer player) {
        ItemStack equipped = player.getHeldItemMainhand();
        if (isAccepted(equipped)) {
            equipped.shrink(1);

            performServer(player);
        }
    }

    private static class Delegate extends KeyDelegate {

        EntityCoinThrowing coin;

        int chargeTicks = -1;

        void informThrowCoin(EntityCoinThrowing coin) {
            if (this.coin == null || this.coin.isDead) {
                this.coin = coin;
                onKeyAbort();
            }
        }

        @Override
        public void onKeyDown() {
            if (coin == null) {
                if (instance.isAccepted(getPlayer().getHeldItemMainhand())) {
                    instance.spawnClientEffect(getPlayer());
                    chargeTicks = 20;
                }
            } else {
                if (coin.getProgress() > 0.7) {
                    NetworkMessage.sendToServer(instance,
                            MSG_COIN_PERFORM, getPlayer(), coin);
                }

                coin = null; // Prevent second QTE judgement
            }
        }

        @Override
        public void onKeyTick() {
            if (chargeTicks != -1) {
                if (--chargeTicks == 0) {
                    NetworkMessage.sendToServer(instance,
                            MSG_ITEM_PERFORM, getPlayer());
                }
            }
        }

        @Override
        public void onKeyUp() {
            chargeTicks = -1;
        }

        @Override
        public void onKeyAbort() {
            chargeTicks = -1;
        }

        public DelegateState getState() {
            if (coin != null && !coin.isDead) {
                return coin.getProgress() < 0.6 ? DelegateState.CHARGE : DelegateState.ACTIVE;
            } else {
                return chargeTicks == -1 ? DelegateState.IDLE : DelegateState.CHARGE;
            }
        }

        @Override
        public ResourceLocation getIcon() {
            return instance.getHintIcon();
        }

        @Override
        public int createID() {
            return 0;
        }

        @Override
        public Skill getSkill() {
            return instance;
        }
    }
}
