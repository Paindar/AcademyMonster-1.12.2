package cn.paindar.academymonster.ability;

import cn.lambdalib2.util.*;
import cn.paindar.academymonster.network.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

import static cn.lambdalib2.util.MathUtils.lerpf;

/**
 * Created by Paindar on 2017/2/17.
 */
public class AMArcGen extends BaseSkill
{
    private float damage;
    private float range ;
    private float prob;
    private float slowdown;
    public AMArcGen(EntityMob speller, float exp)
    {
        super(speller, (int)lerpf(40,20,exp), exp,"electromaster.arc_gen");
        damage=lerpf(1,7,exp);
        range=lerpf(6,15,exp);
        prob=lerpf(0,0.6f,exp);
        slowdown=exp>0.5?lerpf(0,0.8f,exp-0.5f):0;
    }

    public float getMaxDistance(){return range;}

    private Vec3d getDest(EntityLivingBase speller){return Raytrace.getLookingPos(speller, range).getLeft();}

    @Override
    public void start()
    {
        super.start();
        World world=speller.getEntityWorld();
        RayTraceResult result=Raytrace.traceLiving(speller, range, EntitySelectors.living(), BlockSelectors.filNormal);
        switch(result.typeOfHit)
        {
            case ENTITY:
                if (result.entityHit instanceof EntityLivingBase)
                {
                    EntityLivingBase target = (EntityLivingBase) result.entityHit;
                    attack(target, damage);
                    if (RandUtils.nextDouble() <= slowdown)
                    {
                        target.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), 10));
                    }
                }
                break;
            /*case BLOCK:
                if(getSkillExp()>=0.4)
                {
                    BlockPos pos=result.getBlockPos(),
                            abovePos = new BlockPos(pos.getX(), pos.getY()+1, pos.getZ());
                    if (RandUtils.ranged(0, 1) < prob)
                    {
                        if (world.getBlockState(abovePos).getBlock() == Blocks.AIR) {
                            world.setBlockState(abovePos, Blocks.FIRE.getDefaultState());
                        }
                    }
                }
                break;*/
        }
        List<Entity> list= WorldUtils.getEntities(speller, 25, EntitySelectors.player());
        for(Entity e:list)
        {
            NetworkManager.sendArcGenTo(speller,range,(EntityPlayerMP)e);
        }
        cooldown();
    }
}
