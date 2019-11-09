package cn.paindar.academymonster.ability;

import cn.academy.event.BlockDestroyEvent;
import cn.academy.util.Plotter;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.RandUtils;
import cn.lambdalib2.util.VecUtils;
import cn.lambdalib2.util.WorldUtils;
import cn.paindar.academymonster.config.AMConfig;
import cn.paindar.academymonster.network.NetworkManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import java.util.*;
import java.util.function.Predicate;

import static cn.lambdalib2.util.MathUtils.lerpf;
import static cn.lambdalib2.util.RandUtils.rangef;


/**
 * Created by voidcl on 2017/3/20.
 */
public class AMGroundShock extends BaseSkill{
    private static final float groundBreakProb=0.3f;
    private static boolean canBreakBlock;

    @StateEventCallback
    public static void postInit(FMLPostInitializationEvent evt)
    {
        canBreakBlock=AMConfig.getBoolean("am.skill.GroundShock.destroyBlock",true);
    }

    private final float maxDistance;
    private float damage;
    private float dropRate;
    private float ySpeed;
    private float power = 0;

    public AMGroundShock(EntityMob speller, float exp)
    {
        super(speller,(int)lerpf(100,40,exp),exp,"vecmanip.ground_shock");
        maxDistance=lerpf(5,12,exp);
        damage=lerpf(2,8,exp);

        dropRate = lerpf(0.3f, 1.0f, exp);
        ySpeed= rangef(0.6f, 0.9f) * lerpf(0.8f, 1.3f,exp);
        power = lerpf(20, 60, exp);
    }

    public float getMaxDistance()
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
    public void start()
    {
        super.start();
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
                            attack((EntityLivingBase) entity, damage);
                            entity.motionY = ySpeed;
                        }
                    }
                }
            }
            for(int i=1;i<=3;i++)
                breakWithForce(new BlockPos(x, y + i, z), false);
        }

        List<Entity> list= WorldUtils.getEntities(speller, 25, EntitySelectors.player());
        BlockPos[] vecs=new BlockPos[dejavu_blocks.size()];
        for(int i=0;i<dejavu_blocks.size();i++)
        {
            vecs[i]=dejavu_blocks.get(i);
        }

        for(Entity e:list)
        {
            NetworkManager.sendGroundShockEffectTo(vecs, speller, (EntityPlayerMP) e);
        }
        cooldown();
    }
}
