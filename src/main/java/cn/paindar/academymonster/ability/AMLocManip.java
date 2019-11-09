package cn.paindar.academymonster.ability;

import cn.academy.event.BlockDestroyEvent;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.util.BlockSelectors;
import cn.lambdalib2.util.IBlockSelector;
import cn.lambdalib2.util.RandUtils;
import cn.lambdalib2.util.WorldUtils;
import cn.paindar.academymonster.config.AMConfig;
import cn.paindar.academymonster.entity.EntityMagManipBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.lambdalib2.util.MathUtils.lerpf;


/**
 * Created by Paindar on 2017/6/4.
 */
public class AMLocManip extends BaseSkill
{
    private static boolean canDestroyBlock;

    @StateEventCallback
    public static void postInit(FMLPostInitializationEvent evt)
    {
        canDestroyBlock = AMConfig.getBoolean("am.skill.LocManip.destroyBlock",true);
    }

    Map<EntityMagManipBlock,Double> list=new HashMap<>();
    private int time=0;
    private float damage;
    private double lastX,lastY,lastZ;

    public AMLocManip(EntityMob speller, float exp)
    {
        super(speller,(int)lerpf(600,300,exp),exp,"teleporter.loc_manip");
        damage=lerpf(7,10,exp);
    }
    @Override
    public void start()
    {
        super.start();
        list.clear();

        World world=speller.world;

        double range=3;
        double rad=0,part=Math.acos(1.0-9.0/2/(range*range));
        lastX=speller.posX;
        lastY=speller.posY;
        lastZ=speller.posZ;
        List<BlockPos> bpList = new ArrayList<>();
        WorldUtils.getBlocksWithin(bpList, speller, 7, (int)lerpf(25,100,getSkillExp()),
                BlockSelectors.filNormal,
                (world1, x, y, z, block) -> {
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState ibs = world1.getBlockState(pos);
                    float hardness = ibs.getBlockHardness(world1, pos);
                    return hardness>0 && !(ibs.getBlock() instanceof BlockStairs);

                });
        for(BlockPos pos:bpList)
        {
            EntityMagManipBlock entity=  new EntityMagManipBlock(speller, damage,this);
            if(canDestroyBlock && !MinecraftForge.EVENT_BUS.post(new BlockDestroyEvent(world, pos)) )
            {
                entity.setBlock(world.getBlockState(pos).getBlock());
                world.setBlockToAir(pos);
            }
            else
                entity.setBlock(Blocks.ICE);
            entity.setPosition(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
            world.spawnEntity(entity);
            entity.setPlaceFromServer(false);
            entity.radium=rad;
            this.list.put(entity,range);
            rad+=part;
            if(rad>=2*Math.PI)
            {
                part=Math.acos(1-9.0/2/(range*range));
                rad= RandUtils.rangef(0,(float)part);
                range+=2;
            }
        }
    }

    @Override
    public void onTick()
    {
        super.onTick();
        if(!isActivated())
            return;
        time++;
        double tx=0,ty=0,tz=0;
        if(speller==null || speller.isDead)
        {
            cooldown();
            clear();
            return;
        }
        else
        {
            if (time % 3 != 0)
                return;
            tx = speller.posX - lastX;
            ty = speller.posY - lastY;
            tz = speller.posZ - lastZ;
            lastX = speller.posX;
            lastY = speller.posY;
            lastZ = speller.posZ;
        }
        List<EntityMagManipBlock> tmpList = new ArrayList<>();
        for(Map.Entry<EntityMagManipBlock,Double> pair:list.entrySet())
        {
            EntityMagManipBlock entity=pair.getKey();
            if(time>=240)
            {
                clear();
                cooldown();
                break;
            }
            entity.radium=(entity.radium+0.13)%(6.26);
            double rad=entity.radium;
            Vec3d end=new Vec3d(speller.posX+Math.sin(rad)*pair.getValue(),speller.posY+4,speller.posZ+Math.cos(rad)*pair.getValue());
            entity.setPosition(entity.posX+tx,entity.posY+ty,entity.posZ+tz);
            entity.setMoveTo(end.x,end.y,end.z);
        }
    }

    @Override
    public void clear()
    {
        super.clear();
        for(Map.Entry<EntityMagManipBlock, Double> block : list.entrySet())
        {
            block.getKey().setPlaceFromServer(true);
            if(canDestroyBlock)
                block.getKey().stopMoveTo();
            else
                block.getKey().setDead();
        }
        list.clear();
    }


}
