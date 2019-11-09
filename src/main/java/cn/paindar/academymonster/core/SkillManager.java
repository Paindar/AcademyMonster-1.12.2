package cn.paindar.academymonster.core;

import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.lambdalib2.util.RandUtils;
import cn.lambdalib2.util.SideUtils;
import cn.paindar.academymonster.ability.*;
import cn.paindar.academymonster.config.AMConfig;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.lang.reflect.Constructor;
import java.util.*;

import static cn.lambdalib2.util.RandUtils.rangef;


/**
 * Created by Paindar on 2017/3/25.
 */
public class SkillManager
{
    class SkillInfo
    {
        Class<? extends BaseSkill> klass;
        float prob;
        int lvl;
        String name;
        Catalog type;

        @Override
        public String toString()
        {
            return klass + " prob=" + prob +
                     " level = " + lvl + " skill name = " + name +
                    " catalog type = " + type + " at " + super.toString();
        }
    }
    public enum Catalog{vector,meltdown,electro,teleport}
    public static SkillManager instance=new SkillManager();
    private static List<SkillInfo> list=new ArrayList<>();

    private SkillManager(){}

    public BaseSkill createSkillInstance(String skillName, EntityMob speller, float exp)
    {
        BaseSkill skill;
        Class<? extends BaseSkill> skillClass=null;
        for(SkillInfo info:list)
        {
            if(info.name.equals(skillName))
            {
                skillClass=info.klass;
                break;
            }
        }
        if(skillClass==null)
            return null;
        Constructor constructor;
        try
        {
            constructor = skillClass.getConstructor(EntityMob.class, float.class);
            skill = (BaseSkill) constructor.newInstance(speller,exp);
            return skill;
        }
        catch(Exception e)
        {
            AcademyMonster.log.error("No such constructor: (EntityLivingBase.class, float.class)");
            e.printStackTrace();
        }
        return null;
    }

    private void registerSkill(Class<? extends BaseSkill> skill,float defaultProb,int skillLevel,Catalog type)
    {
        float prob=(float) AMConfig.getDouble("am.skill."+skill.getSimpleName().substring(2)+".prob",defaultProb);
        if (prob<=1e-6)
            return ;
        SkillInfo info=new SkillInfo();
        info.klass=skill;
        info.prob=defaultProb;
        info.lvl=skillLevel;
        info.type=type;

        Constructor constructor;
        BaseSkill scill;
        try
        {
            constructor = skill.getConstructor(EntityMob.class, float.class);
            scill = (BaseSkill) constructor.newInstance(null,0);
            info.name=scill.getUnlocalizedSkillName();
        }
        catch(Exception e)
        {
            AcademyMonster.log.error("No such constructor: (EntityLivingBase.class, float.class)");
            e.printStackTrace();
        }
        list.add(info);
        NetworkS11n.register(skill);
    }

    void initSkill()
    {
        registerSkill(AMArcGen.class,1,1,Catalog.electro);
        registerSkill(AMElectronBomb.class, 1,1,Catalog.meltdown);
        registerSkill(AMBodyIntensify.class, 1,1,Catalog.electro);

        registerSkill(AMScatterBomb.class,0.5f,2,Catalog.meltdown);
        registerSkill(AMGroundShock.class,0.5f,2,Catalog.vector);
        registerSkill(AMPenetrateTeleport.class, 1,2,Catalog.teleport);
        registerSkill(AMBloodRetrograde.class,0.8f,2,Catalog.vector);

        registerSkill(AMElectronCurtains.class,0.4f,3,Catalog.meltdown);
        registerSkill(AMLocationTeleport.class,0.7f,3,Catalog.teleport);
        registerSkill(AMThunderBolt.class,0.7f,3,Catalog.electro);
        registerSkill(AMVecReflect.class, 0.3f,3,Catalog.vector);

        registerSkill(AMRailgun.class, 0.2f,4,Catalog.electro);
        registerSkill(AMThunderClap.class,0.4f,4,Catalog.electro);
        registerSkill(AMPlasmaCannon.class,0.3f,4,Catalog.vector);
        registerSkill(AMElectronMissile.class,0.8f,4,Catalog.meltdown);
        registerSkill(AMLocManip.class,0.8f,4,Catalog.teleport);

        list.sort((a,b)->(a.type!=b.type)?(a.type.ordinal()<b.type.ordinal()?-1:1):(a.lvl!=b.lvl?(a.lvl<b.lvl?-1:1):0));
    }


    public void addSkill(EntityMob entity)
    {
        if(entity.getEntityWorld().isRemote)
            return;
        List<String> banList=AMConfig.getStringArray("am.monster."+entity.getClass().getSimpleName()+".ban",new ArrayList<>());
        StringBuilder builder=new StringBuilder();
        {
            double prob=AMConfig.getDouble("am.skill.prob",0.3f);
            double factor=AMConfig.getDouble("am.skill.factor",0.5f);
            double sumWeight=0;
            MobSkillData data = MobSkillData.get(entity);
            data.catalog  = Catalog.values()[RandUtils.nextInt(Catalog.values().length)];
            boolean isTest=false;

            if(!isTest)
            {
                int level = 1, last = 0, mark = 0;
                List<SkillInfo> filtList = new ArrayList<>();

                while (prob >= RandUtils.nextFloat())
                {
                    prob *= factor;
                    SkillInfo info;
                    if (level != last)
                    {
                        for (; mark < list.size(); mark++)
                        {
                            info = list.get(mark);
                            if (!banList.contains(info.klass.getSimpleName().substring(2)))
                            {
                                if (info.lvl <= level)
                                {
                                    filtList.add(info);
                                    sumWeight += info.prob;
                                } else
                                    break;
                            }
                        }
                    }//flush available skill list
                    if (filtList.isEmpty())
                    {
                        if (mark >= list.size())
                        {
                            break;
                        } else
                        {
                            level++;
                            prob /= factor;
                            continue;
                        }
                    }

                    int index = 0;
                    info = filtList.get(0);
                    double p = RandUtils.ranged(0, sumWeight);
                    while (filtList.size() > index)
                    {
                        info = filtList.get(index);
                        if (p < info.prob)
                            break;
                        p -= info.prob;
                        index++;
                    }
                    filtList.remove(index);
                    sumWeight -= info.prob;
                    last = info.lvl;
                    if (info.lvl == level)
                        level++;

                    float randExp = RandUtils.nextFloat();
                    randExp = 0.01f + randExp * randExp;
                    builder.append(info.name).append('~').append(randExp).append('-');
                }
                data.setSkillData(builder.toString());
                data.level = level - 1;
            }
            else
            {
                data.setSkillData("ac.ability.teleporter.threatening_teleport.name~1.00");
                data.level=4;
                data.catalog=Catalog.vector;
            }
        }
    }

}
