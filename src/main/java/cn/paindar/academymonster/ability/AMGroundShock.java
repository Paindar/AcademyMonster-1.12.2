package cn.paindar.academymonster.ability;

import cn.academy.ability.vanilla.generic.client.effect.SmokeEffect;
import cn.academy.event.BlockDestroyEvent;
import cn.academy.util.Plotter;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.lambdalib2.s11n.network.TargetPoints;
import cn.lambdalib2.util.*;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.ability.client.EffectSpawner;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import cn.paindar.academymonster.config.AMConfig;
import cn.paindar.academymonster.network.NetworkManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.function.Predicate;

import static cn.lambdalib2.util.MathUtils.lerp;
import static cn.lambdalib2.util.RandUtils.*;
import static cn.lambdalib2.util.RandUtils.ranged;

public class AMGroundShock extends  SkillTemplate{
    public static final AMGroundShock Instance = new AMGroundShock();
    private static final float groundBreakProb=0.3f;
    private static boolean canBreakBlock;

    @StateEventCallback
    public static void postInit(FMLPostInitializationEvent evt)
    {
        canBreakBlock= AMConfig.getBoolean("am.skill.ground_shock.destroyBlock",true);
    }

    protected AMGroundShock() {
        super("ground_shock");
    }

    @Override
    public MonsterSkillInstance create(Entity e) {
        return new GroupShockContext(e);
    }

    @Override
    public SpellingInfo fromBytes(ByteBuf buf) {
        return null;
    }
    static class GroupShockContext extends MonsterSkillInstance
    {
        private final double maxDistance;
        private final double damage;
        private final double dropRate;
        private final double ySpeed;
        private double power = 0;
        private final int cooldown;

        public GroupShockContext(Entity ent) {
            super(AMGroundShock.Instance, ent);
            maxDistance=lerp(5,12,getExp());
            damage=lerp(2,8,getExp());

            dropRate = lerp(0.3f, 1.0f, getExp());
            ySpeed= rangef(0.6f, 0.9f) * lerp(0.8f, 1.3f,getExp());
            power = lerp(20, 60, getExp());
            cooldown = (int)lerp(100,40,getExp());
        }

        public double getMaxDistance()
        {
            return maxDistance;
        }

        private void breakWithForce(BlockPos pos, boolean drop)
        {
            World world=speller.world;
            IBlockState ibs = world.getBlockState(pos);
            Block block = ibs.getBlock();
            if ( canBreakBlock && !MinecraftForge.EVENT_BUS.post(new BlockDestroyEvent(world, pos)))
            {
                float hardness = ibs.getBlockHardness(world, pos);
                if(hardness >=0 && power>=hardness)
                {
                    if (block != Blocks.FARMLAND && !ibs.getMaterial().isLiquid())
                    {
                        if (drop && RandUtils.nextFloat() < dropRate)
                        {
                            block.dropBlockAsItemWithChance(world, pos, ibs, 1.0f, 0);
                        }
                        world.setBlockToAir(pos);
                        power-=hardness;
                        //world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.PLAYERS, .5f, 1f,false);
                    }
                }
            }
        }

        @Override
        public int execute() {
            Vec3d planeLook = speller.getLookVec().normalize();

            Plotter plotter = new Plotter((int)Math.floor(speller.posX),(int)Math.floor(speller.posY) - 1,
                    (int)Math.floor(speller.posZ), planeLook.x, 0, planeLook.z);

            int iter = (int)maxDistance;

            List<BlockPos> dejavu_blocks = new ArrayList<>();
            List<Entity> dejavu_ent    = new ArrayList<>();

            Vec3d rot = VecUtils.copy(planeLook);
            rot.rotateYaw(90);

            Map<Vec3d,Float> deltas = new HashMap<Vec3d,Float>()
            {
                {
                    put(Vec3d.ZERO,0f);
                    put(rot,0.7f);
                    put(VecUtils.multiply(rot,-1),0.7f);
                    put(VecUtils.multiply(rot,2),0.3f);
                    put(VecUtils.multiply(rot,-2),0.3f);
                }
            };

            Predicate<Entity> selector = EntitySelectors.living().and(EntitySelectors.exclude(speller));
            World world = speller.world;
            while (iter > 0 && power >0)
            {
                int[] next = plotter.next();
                int x=next[0], y=next[1], z=next[2];

                iter -= 1;
                for (Map.Entry<Vec3d,Float> entry: deltas.entrySet())
                {
                    Vec3d delta=entry.getKey();
                    Float prob=entry.getValue();

                    BlockPos pos = new BlockPos(Math.floor(x + delta.x), Math.floor(y + delta.y), Math.floor(z + delta.z));
                    IBlockState ibs = world.getBlockState(pos);
                    Block block = ibs.getBlock();

                    if (RandUtils.nextDouble() < prob)
                    {
                        if (block != Blocks.AIR && !dejavu_blocks.contains(pos))
                        {
                            dejavu_blocks.add(pos);
                            if (block instanceof BlockStone)
                            {
                                world.setBlockState(pos,Blocks.COBBLESTONE.getDefaultState());
                                power -=0.4;
                            } else if (block instanceof BlockGrass)
                            {
                                world.setBlockState(pos, Blocks.DIRT.getDefaultState());
                                power -= 0.2;
                            }
                            else
                                power -= 0.5;
                        }
                        if (RandUtils.nextDouble() < groundBreakProb)
                        {
                            breakWithForce(pos, false);
                        }

                        AxisAlignedBB aabb = new AxisAlignedBB(pos.getX()-0.2, pos.getY()-0.2, pos.getZ()-0.2,
                                pos.getX()+1.4, pos.getY()+2.2, pos.getZ()+1.4);
                        List<Entity> entities = WorldUtils.getEntities(world, aabb, selector);
                        for (Entity entity : entities)
                        {
                            if (!dejavu_ent.contains(entity))
                            {
                                dejavu_ent.add(entity);
                                attack((EntityLivingBase) entity, damage, false);
                                entity.motionY = ySpeed;
                            }
                        }
                    }
                }
                for(int i=1;i<=3;i++)
                    breakWithForce(new BlockPos(x, y + i, z), false);
            }

            GroundShockClientInfo info = new GroundShockClientInfo();
            info.vecs = dejavu_blocks;
            NetworkManager.sendSkillEventAllAround(TargetPoints.convert(speller, 18), speller,
                    Instance, info);
            setDisposed();
            return cooldown;
        }

        @Override
        public void clear() {

        }
    }

    static class GroundShockClientInfo extends SpellingInfo
    {
        List<BlockPos> vecs;
        @Override
        @SideOnly(Side.CLIENT)
        public void action(Entity speller) {
            World world = speller.getEntityWorld();
            for(BlockPos pt:vecs)
            {
                for (int i=0;i<rangei(4, 8);i++)
                {
                    IBlockState is = world.getBlockState(pt);
                    ParticleManager particleManager = Minecraft.getMinecraft().effectRenderer;
                    ParticleDigging particle = (ParticleDigging) Objects.requireNonNull(
                            particleManager.spawnEffectParticle(
                                    EnumParticleTypes.BLOCK_CRACK.getParticleID(),
                                    pt.getX() + nextDouble(), pt.getY() + 1 + nextDouble() * 0.5 + 0.2,
                                    pt.getZ() + nextDouble(),
                                    ranged(-0.2, 0.2), 0.1 + nextDouble() * 0.2, ranged(-0.2, 0.2),
                                    Block.getIdFromBlock(is.getBlock()),
                                    EnumFacing.UP.ordinal()
                            )
                    );
                    particle.setBlockPos(pt);
                }

                if (nextFloat() < 0.5f)
                {
                    SmokeEffect eff = new SmokeEffect(world);
                    Vec3d pos = new Vec3d(pt.getX() + 0.5 + ranged(-.3, .3), pt.getY() + 1 + ranged(0, 0.2), pt.getZ() + 0.5 + ranged(-.3, .3));
                    Vec3d vel = new Vec3d(ranged(-.03, .03), ranged(.03, .06), ranged(-.03, .03));
                    eff.forceSpawn = true;
                    eff.setPosition(pos.x, pos.y, pos.z);
                    eff.motionX = vel.x;
                    eff.motionY = vel.y;
                    eff.motionZ = vel.z;
                    EffectSpawner.Instance.addEffect(eff);
                    //speller.world.spawnEntity(eff);
                }
            }
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            vecs = NetworkS11n.deserialize(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            NetworkS11n.serialize(buf, vecs,false);
        }
    }
}
