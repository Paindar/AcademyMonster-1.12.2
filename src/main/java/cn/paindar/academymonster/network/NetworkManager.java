package cn.paindar.academymonster.network;

import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.lambdalib2.s11n.network.TargetPoints;
import cn.lambdalib2.util.SideUtils;
import cn.paindar.academymonster.ability.SkillTemplate;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.entity.EntityMagManipBlock;
import cn.paindar.academymonster.entity.datapart.MonsterSkillList;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by Paindar on 2017/2/9.
 */
public class NetworkManager
{
    private static SimpleNetworkWrapper instance = NetworkRegistry.INSTANCE.newSimpleChannel(AcademyMonster.MODID);
    private static int nextID = 0;
    private NetworkManager(){}

    @StateEventCallback
    public static void init(FMLPreInitializationEvent event)
    {
        registerMessage(MessageSkillEvent.Handler.class, MessageSkillEvent.class, Side.CLIENT);
        registerMessage(MessageMagManipBlockSync.H.class, MessageMagManipBlockSync.class, Side.CLIENT);
        registerMessage(MessageSyncMobInfo.Handler.class, MessageSyncMobInfo.class, Side.CLIENT);
        registerMessage(MessageSyncMobInfo.Handler.class, MessageSyncMobInfo.class, Side.SERVER);
    }

    @StateEventCallback
    public static void initAdapter(FMLPreInitializationEvent event)
    {
        NetworkS11n.addDirect(BlockPos.class, new NetworkS11n.NetS11nAdaptor<BlockPos>() {
            @Override
            public void write(ByteBuf buf, BlockPos obj) {
                NetworkS11n.serialize(buf, obj.getX(), false);
                NetworkS11n.serialize(buf, obj.getY(), false);
                NetworkS11n.serialize(buf, obj.getZ(), false);
            }

            @Override
            public BlockPos read(ByteBuf buf) throws NetworkS11n.ContextException {
                int x = NetworkS11n.deserialize(buf),
                    y = NetworkS11n.deserialize(buf),
                    z = NetworkS11n.deserialize(buf);
                return new BlockPos(x,y,z);
            }
        });
    }

    private static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(
            Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side)
    {
        instance.registerMessage(messageHandler, requestMessageType, nextID++, side);
    }

    public static void sendSkillEvent(EntityPlayerMP receiver, Entity speller, SkillTemplate skill, SpellingInfo info)
    {
        if(!receiver.getEntityWorld().isRemote)
        {
            MessageSkillEvent msg = new MessageSkillEvent(speller, skill, info);
            instance.sendTo(msg, receiver);
        }
        else
            throw new IllegalStateException("Wrong context side!");
    }
    public static void sendSkillEventAllAround(NetworkRegistry.TargetPoint point, Entity speller, SkillTemplate skill, SpellingInfo info)
    {
        if(!speller.getEntityWorld().isRemote)
        {
            MessageSkillEvent msg = new MessageSkillEvent(speller, skill, info);
            instance.sendToAllAround(msg, point);
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


    public static void syncToAllAround(EntityMob entity, int range, MonsterSkillList list, int level, int cat_id) {
        MessageSyncMobInfo msg = new MessageSyncMobInfo(entity, list, level, cat_id);
        instance.sendToAllAround(msg, TargetPoints.convert(entity, range));
    }
    public static void sendToServer(IMessage msg)
    {
        if(SideUtils.isClient())
        {
            instance.sendToServer(msg);
        }
        else
            throw new IllegalStateException("Wrong context side!");
    }

    public static void sendToClient(EntityPlayerMP client, IMessage msg)
    {
        if(!SideUtils.isClient())
        {
            instance.sendTo(msg, client);
        }
        else
            throw new IllegalStateException("Wrong context side!");
    }
}
