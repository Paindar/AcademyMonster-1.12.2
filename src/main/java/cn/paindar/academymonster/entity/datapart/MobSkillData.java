package cn.paindar.academymonster.entity.datapart;

import cn.academy.event.ability.CalcEvent;
import cn.lambdalib2.datapart.DataPart;
import cn.lambdalib2.datapart.EntityData;
import cn.lambdalib2.datapart.RegDataPart;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.registry.mc.RegEventHandler;
import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.paindar.academymonster.api.INbt;
import cn.paindar.academymonster.core.SkillManager;
import cn.paindar.academymonster.network.MessageSyncMobInfo;
import cn.paindar.academymonster.network.NetworkManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


/**
 * Created by Paindar on 2017/2/15.
 */
@RegDataPart(EntityMob.class)
public class MobSkillData extends DataPart<EntityMob> implements INbt
{
    public static MobSkillData get(EntityMob e)
    {
        return EntityData.get(e).getPart(MobSkillData.class);
    }
    @StateEventCallback
    public static void onInit(FMLInitializationEvent evt)
    {
        NetworkS11n.addDirect(MobSkillData.class, new NetworkS11n.NetS11nAdaptor<MobSkillData>() {
            @Override
            public void write(ByteBuf buf, MobSkillData obj) {
                obj.list.toBytes(buf);
                NetworkS11n.serialize(buf, obj.level, false);
                NetworkS11n.serialize(buf, obj.catalog.ordinal(), true);
            }

            @Override
            public MobSkillData read(ByteBuf buf) throws NetworkS11n.ContextException {
                MobSkillData data = new MobSkillData();
                data.list.fromBytes(buf);
                data.level = NetworkS11n.deserialize(buf);
                int cat_id = NetworkS11n.deserialize(buf);
                data.catalog = SkillManager.Catalog.values()[cat_id];
                return data;
            }
        });
    }
    private final MonsterSkillList list = new MonsterSkillList();
    public int level=0;
    public SkillManager.Catalog catalog = SkillManager.Catalog.none;
    private boolean locked = false;
    private int interferTicks = 0;
    private double enhancement = 0.00;

    public MobSkillData()
    {
        setTick(true);
        setNBTStorage();
    }

    @Override
    public void tick() {
        super.tick();
        if(interferTicks>0)
            interferTicks-=1;
        list.tick(isInterfered());
        if(!locked && isClient())
        {
            MessageSyncMobInfo info = new MessageSyncMobInfo(getEntity(), null, 0,0);
            NetworkManager.sendToServer(info);
            locked = true;
        }
    }

    // used for AIM Scanner's info sync.
    public MonsterSkillList getSkillData(){return list;}
    public void interfer(int ticks){interferTicks+=ticks;}
    public boolean isInterfered(){return interferTicks>0;}
    @Override
    public void toNBT(NBTTagCompound tag) {
        //NBTS11n.write(tag, this);
        NBTTagCompound nbt = new NBTTagCompound();
        list.toNBT(nbt);
        tag.setTag("list", nbt);
        tag.setInteger("level", level);
        tag.setInteger("cat", catalog.ordinal());
        tag.setDouble("enh", enhancement);
    }

    @Override
    public void fromNBT(NBTTagCompound tag) {
        if(!locked)
        {
            list.fromNBT((NBTTagCompound) tag.getTag("list"));
            level = tag.getInteger("level");
            catalog = SkillManager.Catalog.values()[tag.getInteger("cat")];
            enhancement = tag.getDouble("enh");
            init();
        }
    }

    public void init()
    {
        //Data in client will be empty until server sync.
        if(getEntity().getEntityWorld().isRemote)
        {
            return;
        }
        locked = true;
        list.init();
    }

    @Override
    public void onPlayerDead() {
        super.onPlayerDead();
        list.clear();
    }

    public boolean isLocked(){return locked;}


    private void clear()
    {
        list.clear();
    }

    public double getEnhancement() {
        return enhancement;
    }


    public enum Events {
        @RegEventHandler()
        instance;

        @SubscribeEvent
        public void onPlayerCauseDamage(CalcEvent.SkillAttack evt)
        {
            if(evt.target instanceof EntityMob)
            {
                MobSkillData data = MobSkillData.get((EntityMob) evt.target);
                evt.targetEnhancement = data.enhancement;
            }
        }
    }
}
