package cn.paindar.academymonster.ability;

import cn.academy.event.BlockDestroyEvent;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.util.BlockSelectors;
import cn.lambdalib2.util.RandUtils;
import cn.lambdalib2.util.WorldUtils;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import cn.paindar.academymonster.config.AMConfig;
import cn.paindar.academymonster.entity.EntityMagManipBlock;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
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

import static cn.lambdalib2.util.MathUtils.lerp;

public class AMLocManip extends SkillTemplate
{
    public static final AMLocManip Instance = new AMLocManip();
    public static boolean canDestroyBlock = true;

    @StateEventCallback
    public static void postInit(FMLPostInitializationEvent evt)
    {
        //need debugging
        canDestroyBlock = AMConfig.getBoolean("am.skill.loc_manip.destroyBlock",true);
    }

    protected AMLocManip() {
        super("loc_manip");
    }


    @Override
    public MonsterSkillInstance create(Entity e) {
        return new AMLocManip.LocManipContext(e);
    }

    @Override
    public SpellingInfo fromBytes(ByteBuf buf) {
        return null;
    }

    static class LocManipContext extends MonsterSkillInstance
    {
        Map<EntityMagManipBlock,Double> list=new HashMap<>();
        private int time=0;
        private final double  damage;
        private double lastX,lastY,lastZ;
        private final int cooldown;

        public LocManipContext(Entity ent) {
            super(AMLocManip.Instance, ent);
            damage = lerp(7,10,getExp());
            cooldown = (int)lerp(600,300,getExp());
        }

        @Override
        public int execute() {
            list.clear();

            World world=speller.world;

            double range=3;
            double rad=0,part=Math.acos(1.0-9.0/2/(range*range));
            lastX=speller.posX;
            lastY=speller.posY;
            lastZ=speller.posZ;
            List<BlockPos> bpList = new ArrayList<>();
            WorldUtils.getBlocksWithin(bpList, speller, 7, (int)lerp(25,100,getExp()),
                    BlockSelectors.filNormal,
                    (world1, x, y, z, block) -> {
                        BlockPos pos = new BlockPos(x, y, z);
                        IBlockState ibs = world1.getBlockState(pos);
                        float hardness = ibs.getBlockHardness(world1, pos);
                        return hardness>0 && !(ibs.getBlock() instanceof BlockStairs);

                    });
            for(BlockPos pos:bpList)
            {
                EntityMagManipBlock entity=  new EntityMagManipBlock(speller, (float) damage,this);
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

            return WAITING ;
        }

        @Override
        public void tick()
        {
            time++;
            double tx=0,ty=0,tz=0;
            if(speller.isDead)
            {
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
            for(Map.Entry<EntityMagManipBlock,Double> pair:list.entrySet())
            {
                EntityMagManipBlock entity=pair.getKey();
                if(time>=240)
                {
                    clear();
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
            setDisposed();
            MobSkillData.get((EntityMob) speller).getSkillData().setCooldown(template, cooldown);
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
}
