package cn.paindar.academymonster.network;

import cn.academy.ability.vanilla.generic.client.effect.SmokeEffect;
import cn.academy.client.sound.ACSounds;
import cn.lambdalib2.s11n.nbt.NBTS11n;
import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.lambdalib2.util.SideUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sun.nio.ch.Net;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.lambdalib2.util.RandUtils.*;

/**
 * Created by Paindar on 2017/3/21.
 */
public class MessageGroundShockEffect implements IMessage, IMsgAction
{
    @Override
    public boolean execute() {
        World world = SideUtils.getWorld(nbt.getInteger("world"));
        EntityMob mob = (EntityMob) world.getEntityByID(nbt.getInteger("entity"));
        ACSounds.playClient(mob, "vecmanip.groundshock", SoundCategory.HOSTILE, 2);

        int[] bytes=nbt.getIntArray("vecs");
        BlockPos[] vecs=new BlockPos[bytes.length/3];
        for(int i=0;i<vecs.length;i++)
        {
            vecs[i]=new BlockPos(bytes[i*3],bytes[i*3+1],bytes[i*3+2]);
        }

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
                world.spawnEntity(eff);
            }
        }

        return true;
    }

    public static class Handler implements IMessageHandler<MessageGroundShockEffect, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageGroundShockEffect msg, MessageContext ctx)
        {
            if (ctx.side == Side.CLIENT)
            {
                NetworkManager.addAction(msg);
            }
            return null;
        }
    }
    NBTTagCompound nbt;
    public MessageGroundShockEffect(){}
    MessageGroundShockEffect(BlockPos[] vecs, EntityMob mob)
    {
        int[] bytes=new int[vecs.length*3];
        nbt=new NBTTagCompound();
        for(int i=0;i<vecs.length;i++)
        {
            bytes[3*i]=vecs[i].getX();
            bytes[3*i+1]=vecs[i].getY();
            bytes[3*i+2]=vecs[i].getZ();
        }
        nbt.setInteger("world", mob.dimension);
        nbt.setInteger("entity", mob.getEntityId());
        nbt.setIntArray("vecs",bytes);
    }
    /**
     * Convert from the supplied buffer into your specific message type
     *
     * @param buf
     */
    @Override
    public void fromBytes(ByteBuf buf)
    {
        nbt= ByteBufUtils.readTag(buf);
    }

    /**
     * Deconstruct your message into the supplied byte buffer
     *
     * @param buf
     */
    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, nbt);
    }
}
