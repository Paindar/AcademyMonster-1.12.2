package cn.paindar.academymonster.entity.ai;

import cn.paindar.academymonster.ability.AMPenetrateTeleport;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


/**
 * Created by Paindar on 2017/2/9.
 */
public class EntityAIPenetrateTeleport extends EntityAIBaseX
{
    AMPenetrateTeleport skill;
    EntityLivingBase target;

    public EntityAIPenetrateTeleport(EntityLivingBase tgt, AMPenetrateTeleport skill)
    {
        super();
        target=tgt;
        this.skill=skill;
    }

    private boolean hasPlace(World world ,double  x,double y,double z)
    {
        int ix=(int)x,iy=(int)y,iz=(int)z;
        BlockPos pos1 = new BlockPos(ix, iy, iz), pos2 = new BlockPos(ix, iy+1, iz);
        IBlockState ibs1 = world.getBlockState(pos1), ibs2 = world.getBlockState(pos2);
        Block b1 = ibs1.getBlock(), b2 = ibs2.getBlock();
        return !b1.canCollideCheck(ibs1, false) && !b2.canCollideCheck(ibs2, false);
    }

    @Override
    public boolean execute(EntityMob owner)
    {
        double dist=Math.sqrt(owner.getDistanceSq(target));
        double distBtwEntitiess = dist;
        dist = dist>skill.getMaxDistance()?skill.getMaxDistance():dist;
        if(target!=null && !skill.isSkillInCooldown() && dist >=0.5)
        {

            double dx= (target.posX-owner.posX)/distBtwEntitiess,
                    dy=(target.posY-owner.posY)/distBtwEntitiess,
                    dz=(target.posZ-owner.posZ)/distBtwEntitiess;
            World world=owner.world;
            for(double d=dist;d>0;d-=1)
            {
                double x = owner.posX + dx * d;
                double y = owner.posY + dy * d;
                double z = owner.posZ + dz * d;
                if(hasPlace(world,x,y,z))
                {
                    this.skill.startTeleport(x,y,z);
                    owner.getNavigator().clearPath();
                    break;
                }
                else if(hasPlace(world,x,y+1,z))
                {
                    this.skill.startTeleport(x,y+1,z);
                    break;
                }
            }
        }
        MobSkillData.get(owner).setAI(new EntityAIChasing(target,40));
        return true;
    }
}
