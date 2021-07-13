package cn.paindar.academymonster.ability;

import cn.academy.client.sound.ACSounds;
import cn.lambdalib2.s11n.network.TargetPoints;
import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.RandUtils;
import cn.lambdalib2.util.WorldUtils;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import cn.paindar.academymonster.network.NetworkManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

import static cn.lambdalib2.util.MathUtils.lerp;

public class AMLocationTeleport extends SkillTemplate{
    public static final AMLocationTeleport Instance = new AMLocationTeleport();
    protected AMLocationTeleport()
    {
        super("location_teleport");
    }
    @Override
    public MonsterSkillInstance create(Entity e) {
        return new LocationTeleportContext(e);
    }

    @Override
    public SpellingInfo fromBytes(ByteBuf buf) {
        LocationTeleportClientInfo info = new LocationTeleportClientInfo();
        info.fromBytes(buf);
        return info;
    }
    static class LocationTeleportContext extends MonsterSkillInstance
    {
        private final int dropHeight;
        private final int InGround=2;
        private final int maxDistance;
        private final int cooldown;
        public LocationTeleportContext(Entity ent) {
            super(Instance, ent);
            maxDistance = (int)lerp(2,4,exp);
            dropHeight=(int)lerp(7,20,exp);
            cooldown = (int)lerp(120, 60, getExp());
        }
        private boolean hasPlace(World world , double  x, double y, double z)
        {
            int ix=(int)x,iy=(int)y,iz=(int)z;
            BlockPos pos1 = new BlockPos(ix, iy, iz), pos2 = new BlockPos(ix, iy+1, iz);
            IBlockState ibs1 = world.getBlockState(pos1), ibs2 = world.getBlockState(pos2);
            Block b1 = ibs1.getBlock(), b2 = ibs2.getBlock();
            return !b1.canCollideCheck(ibs1, false) && !b2.canCollideCheck(ibs2, false);
        }

        private boolean SkyOrGround(Entity target)
        {
            return hasPlace(speller.world, target.posX,target.posY+10,target.posZ);
        }

        public int getMaxDistance(){return maxDistance-1;}

        @Override
        public int execute() {
            int rand= RandUtils.nextInt(1)-RandUtils.nextInt(1);
            List<Entity> entityList = WorldUtils.getEntities(speller, maxDistance, EntitySelectors.living());
            if(!entityList.isEmpty())
            {
                LocationTeleportClientInfo info = new LocationTeleportClientInfo();
                if(SkyOrGround(speller))
                {
                    Vec3d pos = new Vec3d(speller.posX, speller.posY+dropHeight, speller.posZ);
                    for(Entity entity: entityList)
                    {
                        entity.setPosition(pos.x + rand, pos.y, pos.z + rand);
                        if(entity instanceof EntityPlayerMP)
                            NetworkManager.sendSkillEvent((EntityPlayerMP) entity, speller, Instance, info);
                    }
                }
                else
                {
                    Vec3d pos = new Vec3d(speller.posX, speller.posY-InGround, speller.posZ);
                    for(Entity entity: entityList)
                    {
                        entity.setPosition(pos.x + rand, pos.y, pos.z + rand);
                        if(entity instanceof EntityPlayerMP)
                            NetworkManager.sendSkillEvent((EntityPlayerMP) entity, speller, Instance, info);
                    }
                }
            }
            setDisposed();
            return cooldown;
        }

        @Override
        public void clear() {

        }
    }
    static class LocationTeleportClientInfo extends SpellingInfo
    {
        @Override
        public void action(Entity speller) {
            ACSounds.playClient(speller.world, speller.posX, speller.posY, speller.posZ, "tp.tp",
                    SoundCategory.HOSTILE, .5f, 1f);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
        }

        @Override
        public void toBytes(ByteBuf buf) {
        }
    }
}
