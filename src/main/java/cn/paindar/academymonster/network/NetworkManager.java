package cn.paindar.academymonster.network;

import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.s11n.nbt.NBTS11n;
import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.lambdalib2.util.SideUtils;
import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.entity.EntityMagManipBlock;
import cn.paindar.academymonster.entity.EntityPlasmaBodyEffect;
import cn.paindar.academymonster.entity.EntityTornadoEffect;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Paindar on 2017/2/9.
 */
public class NetworkManager
{
    private static SimpleNetworkWrapper instance = NetworkRegistry.INSTANCE.newSimpleChannel(AcademyMonster.MODID);
    private static int nextID = 0;
    private static List<IMsgAction> msgActions = Collections.synchronizedList(new ArrayList<>());
    private NetworkManager(){}

    public static void addAction(IMsgAction act)
    {
        msgActions.add(act);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent evt)
    {
        if(evt.phase== TickEvent.Phase.START)
        {
            if(Minecraft.getMinecraft().world!=null)
                msgActions.removeIf(IMsgAction::execute);
            else
                msgActions.clear();
        }
    }

    @StateEventCallback
    public static void init(FMLPreInitializationEvent event)
    {
        registerMessage(MessageSound.H.class, MessageSound.class, Side.CLIENT);
        registerMessage(MessageMdRayEffect.H.class, MessageMdRayEffect.class, Side.CLIENT);
        registerMessage(MessageRailgunEffect.H.class, MessageRailgunEffect.class, Side.CLIENT);
        registerMessage(MessageSkillInfoSync.H.class, MessageSkillInfoSync.class,Side.CLIENT);
        registerMessage(MessageArcGenEffect.H.class, MessageArcGenEffect.class,Side.CLIENT);
        registerMessage(MessageThunderBolt.H.class, MessageThunderBolt.class,Side.CLIENT);
        registerMessage(MessageGroundShockEffect.Handler.class, MessageGroundShockEffect.class,Side.CLIENT);
        registerMessage(MessageMagManipBlockSync.H.class, MessageMagManipBlockSync.class,Side.CLIENT);
        registerMessage(MessagePlasmaEffectSync.H.class,MessagePlasmaEffectSync.class,Side.CLIENT);
        registerMessage(MessageFleshRippingEffect.H.class,MessageFleshRippingEffect.class,Side.CLIENT);
        if(SideUtils.isClient())
        {
            MinecraftForge.EVENT_BUS.register(new NetworkManager());
        }
    }


    private static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(
            Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side)
    {
        instance.registerMessage(messageHandler, requestMessageType, nextID++, side);
    }

    public static void sendFleshRippingEffectTo(EntityLivingBase target, EntityPlayerMP player)
    {
        if(!player.getEntityWorld().isRemote)
        {
            MessageFleshRippingEffect msg = new MessageFleshRippingEffect(target);
            instance.sendTo(msg, player);
        }
        else
            throw new IllegalStateException("Wrong context side!");
    }

    public static void sendSoundTo(String sound,EntityLivingBase source,float vol, EntityPlayerMP player)
    {
        if(!player.getEntityWorld().isRemote)
        {
            MessageSound msg = new MessageSound(sound, source,vol);
            instance.sendTo(msg, player);
        }
        else
            throw new IllegalStateException("Wrong context side!");
    }

    public static void sendMdRayEffectTo(Vec3d str, Vec3d end, EntityMob mob, EntityPlayerMP player)
    {
        if(!player.getEntityWorld().isRemote)
        {
            MessageMdRayEffect msg = new MessageMdRayEffect(str,end, mob);
            instance.sendTo(msg, player);
        }
        else
            throw new IllegalStateException("Wrong context side!");
    }

    public static void sendRailgunEffectTo(EntityMob speller, Vec3d str,Vec3d end, EntityPlayerMP player)
    {
        if(!player.getEntityWorld().isRemote)
        {
            MessageRailgunEffect msg = new MessageRailgunEffect(speller, str, end);
            instance.sendTo(msg, player);
        }
        else
            throw new IllegalStateException("Wrong context side!");
    }


    public static void sendEntitySkillInfoTo(EntityMob entity, EntityPlayerMP player)
    {
        if(!player.getEntityWorld().isRemote)
        {
            MessageSkillInfoSync msg = new MessageSkillInfoSync(entity);
            instance.sendTo(msg, player);
        }
        else
            throw new IllegalStateException("Wrong context side!");
    }

    public static void sendArcGenTo(EntityLivingBase speller,float range, EntityPlayerMP player)
    {
        if(!player.getEntityWorld().isRemote)
        {
            MessageArcGenEffect msg = new MessageArcGenEffect(speller,range);
            instance.sendTo(msg, player);
        }
        else
            throw new IllegalStateException("Wrong context side!");
    }

    public static void sendThunderBoltTo(EntityMob source,Vec3d target,List<Entity> list, EntityPlayerMP player)
    {
        if(!player.getEntityWorld().isRemote)
        {
            MessageAutos11n msg = new MessageThunderBolt(source,target,list);
            instance.sendTo(msg, player);
        }
        else
            throw new IllegalStateException("Wrong context side!");
    }
    public static void sendGroundShockEffectTo(BlockPos[] vecs, EntityMob mob, EntityPlayerMP player)
    {
        if(!player.getEntityWorld().isRemote)
        {
            MessageGroundShockEffect msg = new MessageGroundShockEffect(vecs, mob);
            instance.sendTo(msg, player);
        }
        else
            throw new IllegalStateException("Wrong context side!");
    }
    public static void sendMagToAllAround(NetworkRegistry.TargetPoint point, EntityMagManipBlock entity,boolean value)
    {
        if(!entity.world.isRemote)
        {
            MessageMagManipBlockSync msg = new MessageMagManipBlockSync(entity,value);
            instance.sendToAllAround(msg, point);
        }
        else
            throw new IllegalStateException("Wrong context side!");
    }

    public static void sendPlasmaStateChange(NetworkRegistry.TargetPoint point, EntityPlasmaBodyEffect entity)
    {
        if(!entity.world.isRemote)
        {
            MessagePlasmaEffectSync msg = new MessagePlasmaEffectSync(entity);
            instance.sendToAllAround(msg, point);
        }
        else
            throw new IllegalStateException("Wrong context side!");
    }
    public static void sendPlasmaStateChange(NetworkRegistry.TargetPoint point, EntityTornadoEffect entity)
    {
        if(!entity.world.isRemote)
        {
            MessagePlasmaEffectSync msg = new MessagePlasmaEffectSync(entity);
            instance.sendToAllAround(msg, point);
        }
        else
            throw new IllegalStateException("Wrong context side!");
    }
}
