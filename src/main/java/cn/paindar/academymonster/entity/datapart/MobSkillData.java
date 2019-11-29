package cn.paindar.academymonster.entity.datapart;

import cn.academy.event.ability.CalcEvent;
import cn.lambdalib2.datapart.DataPart;
import cn.lambdalib2.datapart.EntityData;
import cn.lambdalib2.datapart.RegDataPart;
import cn.lambdalib2.registry.mc.RegEventHandler;
import cn.lambdalib2.s11n.SerializeExcluded;
import cn.lambdalib2.s11n.SerializeIncluded;
import cn.lambdalib2.s11n.nbt.NBTS11n;
import cn.paindar.academymonster.ability.BaseSkill;
import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.core.SkillManager;
import cn.paindar.academymonster.entity.ai.EntityAIBaseX;
import cn.paindar.academymonster.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paindar on 2017/2/15.
 */
@RegDataPart(EntityMob.class)
public class MobSkillData extends DataPart<EntityMob>
{
    public static MobSkillData get(EntityMob e)
    {
        return EntityData.get(e).getPart(MobSkillData.class);
    }

    @SerializeIncluded
    private String skillData="";
    @SerializeIncluded
    public int level=0;

    @SerializeExcluded
    public List<BaseSkill> list=new ArrayList<>();
    @SerializeIncluded
    public SkillManager.Catalog catalog;
    @SerializeExcluded
    private int time=0;
    @SerializeExcluded
    private EntityAIBaseX ai=null;
    @SerializeExcluded
    private boolean locked = false;
    @SerializeExcluded
    private double enchancement = 0.00;

    public MobSkillData()
    {
        setTick(true);
        setNBTStorage();
        setClientNeedSync();
    }

    @Override
    public void tick()
    {
        time++;
        for(BaseSkill skill :list)
        {
            skill.onTick();
        }
        if(time>=10)
        {
            if(ai!=null)
                ai.execute(getEntity());
            time=0;
        }

    }

    public void setSkillData(String data)
    {
        skillData=data;
    }

    // used for AIM Scanner's info sync.
    public String getSkillData(){return skillData;}// used for AIM Scanner's info sync.

    public void setAI(EntityAIBaseX ai)//update AI, fired in initialization and update AI action.
    {
        this.ai=ai;
    }

    @Override
    public void toNBT(NBTTagCompound tag) {
        NBTS11n.write(tag, this);
    }

    @Override
    public void fromNBT(NBTTagCompound tag) {
        NBTS11n.read(tag, this);
        if(!locked)
        {
            init();
        }
    }

    public void init()
    {
        if(getEntity().getEntityWorld().isRemote)
            return;
        String[] strList=skillData.split("-");
        for(String name:strList)
        {
            String[] skillInfo=name.split("~");
            float exp;
            if(skillInfo.length!=2)
            {
                continue;
            }
            try
            {
                exp=Float.parseFloat(skillInfo[1]);
            }
            catch(Exception e)
            {
                AcademyMonster.log.warn("Failed to translate "+getEntity() + " in "+skillInfo[0]+"  "+skillInfo[1]);
                exp=0;
            }
            BaseSkill skill = SkillManager.instance.createSkillInstance(skillInfo[0], getEntity(), exp);
            //SkillManager.instance.addSkillAI(skill,(EntityLiving) speller);
            if(skill!=null)list.add(skill);
        }
        locked=true;
        setAI(new EntityAIWander());
    }

    public boolean isLocked(){return locked;}

    /**
     * release all data about SEEP
     */
    public void clear()
    {
        for(BaseSkill skill:list)
        {
            skill.clear();
        }
        list.clear();
        ai=null;
    }

    public double getEnchancement() {
        return enchancement;
    }

    public enum Events {
        @RegEventHandler()
        instance;

        @SubscribeEvent
        public void onMobDeath(LivingDeathEvent evt)
        {
            if (evt.getEntityLiving() instanceof EntityMob)
            {
                MobSkillData data =  MobSkillData.get((EntityMob) evt.getEntityLiving());
                data.clear();
            }
        }


        @SubscribeEvent
        public void onPlayerCauseDamage(CalcEvent.SkillAttack evt)
        {
            if(evt.target instanceof EntityMob)
            {
                MobSkillData data = MobSkillData.get((EntityMob) evt.target);
                evt.targetEnhancement = data.enchancement;
            }
        }
    }
}
