package cn.paindar.academymonster.ability;

import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.RandUtils;
import cn.lambdalib2.util.WorldUtils;
import cn.paindar.academymonster.network.NetworkManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

import static cn.lambdalib2.util.MathUtils.lerpf;


/**
 * Created by voidcl on 2017/3/20.
 */
public class AMLocationTeleport extends BaseSkill {

    private int dropHeight;
    private int InGround=2;
    private int maxDistance;

    public AMLocationTeleport(EntityMob speller, float exp)
    {
        super(speller,(int)lerpf(120,60,exp),exp,"teleporter.location_teleport");
        maxDistance = (int)lerpf(3,6,exp);
        dropHeight=(int)lerpf(7,20,exp);
    }

    private boolean hasPlace(World world ,double  x,double y,double z)
    {
        int ix=(int)x,iy=(int)y,iz=(int)z;
        BlockPos pos1 = new BlockPos(ix, iy, iz), pos2 = new BlockPos(ix, iy+1, iz);
        IBlockState ibs1 = world.getBlockState(pos1), ibs2 = world.getBlockState(pos2);
        Block b1 = ibs1.getBlock(), b2 = ibs2.getBlock();
        return !b1.canCollideCheck(ibs1, false) && !b2.canCollideCheck(ibs2, false);
    }

    private boolean SkyOrGround(EntityLivingBase target)
    {
        return hasPlace(speller.world, target.posX,target.posY+10,target.posZ);
    }

    public int getMaxDistance(){return maxDistance-1;}

    @Override
    public void start()
    {
        super.start();
        int rand= RandUtils.nextInt(1)-RandUtils.nextInt(1);
        List<Entity> entityList = WorldUtils.getEntities(speller, maxDistance, EntitySelectors.living());
        if(!entityList.isEmpty())
        {
            if(SkyOrGround(speller))
            {
                Vec3d pos = new Vec3d(speller.posX, speller.posY+dropHeight, speller.posZ);
                for(Entity entity: entityList)
                {
                    entity.setPosition(pos.x + rand, pos.y, pos.z + rand);
                }
            }
            else
            {
                Vec3d pos = new Vec3d(speller.posX, speller.posY-InGround, speller.posZ);
                for(Entity entity: entityList)
                {
                    entity.setPosition(pos.x + rand, pos.y, pos.z + rand);
                }
            }
            List<Entity> list= WorldUtils.getEntities(speller, 25, EntitySelectors.player());
            for(Entity e:list)
            {
                NetworkManager.sendSoundTo("tp.tp", speller,.5f,(EntityPlayerMP)e);
            }
        }
        cooldown();
    }

}
