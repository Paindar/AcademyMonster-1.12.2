package cn.paindar.academymonster.network;

import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.lambdalib2.util.SideUtils;
import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.entity.EntityMagManipBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paindar on 2017/6/5.
 */
public class MessageMagManipBlockSync implements IMessage
{
    public static class H implements IMessageHandler<MessageMagManipBlockSync, IMessage>
    {
        @Override
        public IMessage onMessage(MessageMagManipBlockSync message, MessageContext ctx) {
            add(message.entityId, message.value);
            return null;
        }
    }
    private static class EBPair {
        public int entityId;
        public boolean value;

        public EBPair(){}
        public EBPair(int entityId, boolean value) {
            this.entityId = entityId;
            this.value = value;
        }
    }
    public static class MMBListener
    {
        public static final MMBListener Instance = new MMBListener();

        @StateEventCallback
        public static void onPostInit(FMLPostInitializationEvent evt)
        {
            if(SideUtils.isClient())
                MinecraftForge.EVENT_BUS.register(Instance);
        }

        public final List<EBPair> todoist = new ArrayList<>();
        @SubscribeEvent
        public void onTick(TickEvent evt)
        {
            if(evt.phase!= TickEvent.Phase.END)
            {
                return;
            }
            synchronized (todoist)
            {
                if(!todoist.isEmpty())
                {
                    List<EBPair> removing = new ArrayList<>();
                    World world = Minecraft.getMinecraft().world;
                    if(world==null)
                    {
                        todoist.clear();
                        return;
                    }
                    for(EBPair kv : todoist)
                    {
                        Entity e = world.getEntityByID(kv.entityId);
                        if(e!=null)
                        {
                            if(e instanceof EntityMagManipBlock)
                                ((EntityMagManipBlock)e).setPlaceWhenCollide(kv.value);
                            else
                                AcademyMonster.log.warn("Try cast an entity[%s] to EntityMagManipBlock!");
                            removing.add(kv);
                        }
                    }
                    todoist.removeAll(removing);
                }
            }
        }

    }

    int entityId;
    boolean value;
    public MessageMagManipBlockSync(){}
    public MessageMagManipBlockSync(EntityMagManipBlock entity, boolean value)
    {
        this.entityId = entity.getEntityId();
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = NetworkS11n.deserialize(buf);
        value = NetworkS11n.deserialize(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkS11n.serialize(buf, entityId,false);
        NetworkS11n.serialize(buf, value, false);
    }
    private static void add(int entityId, boolean value)
    {
        synchronized (MMBListener.Instance.todoist)
        {
            MMBListener.Instance.todoist.add(new EBPair(entityId, value));
        }
    }

}
