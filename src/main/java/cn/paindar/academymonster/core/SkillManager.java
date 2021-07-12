package cn.paindar.academymonster.core;

import cn.lambdalib2.util.RandUtils;
import cn.paindar.academymonster.ability.*;
import cn.paindar.academymonster.config.AMConfig;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import cn.paindar.academymonster.entity.datapart.MonsterSkillList;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityMob;

import java.util.*;


/**
 * Created by Paindar on 2017/3/25.
 */
public class SkillManager
{
    static class SkillInfo
    {
        SkillTemplate template;
        float prob;
        int lvl;
        String name;
        Catalog type;

        @Override
        public String toString()
        {
            return String.format("skill_name: %s\nprob: %f\nlevel: %d\ncatalog:%s\n",
                    template.getId(), prob, lvl, type);
        }
    }
    public enum Catalog{none, vector,meltdown,electro,teleport}
    public static SkillManager instance=new SkillManager();
    private static List<SkillInfo> list=new ArrayList<>();

    private SkillManager(){}

    public SkillTemplate getSkillTemplate(String id)
    {
        Optional<SkillInfo> ret = list.stream()
                .filter((SkillInfo info)-> info.template.getId().equals(id))
                .findAny();
        return ret.map(skillInfo -> skillInfo.template).orElse(null);
    }

    private void registerSkill(SkillTemplate skill, float defaultProb, int skillLevel, Catalog type)
    {
        float prob=(float) AMConfig.getDouble("am.skill."+skill.getId()+".prob", defaultProb);
        skillLevel = AMConfig.getInt("am.skill."+skill.getId()+".level", skillLevel);
        if (prob<=1e-6)
            return ;
        SkillInfo info=new SkillInfo();
        info.template=skill;
        info.prob=prob;
        info.lvl=skillLevel;
        info.type=type;

        list.add(info);
    }

    void initSkill()
    {
        registerSkill(AMArcGen.Instance,1,1,Catalog.electro);
        registerSkill(AMElectronBomb.Instance, 1,1,Catalog.meltdown);
        registerSkill(AMBodyIntensify.Instance, 1,1,Catalog.electro);

        registerSkill(AMScatterBomb.Instance,0.5f,2,Catalog.meltdown);
        registerSkill(AMGroundShock.Instance,0.5f,2,Catalog.vector);
        registerSkill(AMPenetrateTeleport.Instance, 1,2,Catalog.teleport);
        registerSkill(AMBloodRetro.Instance,0.8f,2,Catalog.vector);

        registerSkill(AMElectronCurtains.Instance,0.4f,3,Catalog.meltdown);
        registerSkill(AMLocationTeleport.Instance,0.7f,3,Catalog.teleport);
        registerSkill(AMThunderBolt.Instance,0.7f,3,Catalog.electro);
        registerSkill(AMVecReflect.Instance, 0.3f,3,Catalog.vector);
//
        registerSkill(AMRailgun.Instance, 0.2f,4,Catalog.electro);
//        registerSkill(AMThunderClap.class,0.4f,4,Catalog.electro);//Being replaced with E.M.P.
        registerSkill(AMPlasmaCannon.Instance,0.3f,4,Catalog.vector);
        registerSkill(AMElectronMissile.Instance,0.8f,4,Catalog.meltdown);
        registerSkill(AMLocManip.Instance,0.8f,4,Catalog.teleport);
        list.sort((a,b)->(a.lvl!=b.lvl?(a.lvl<b.lvl?-1:1):0));
        //list.sort((a,b)->(a.type!=b.type)?(a.type.ordinal()<b.type.ordinal()?-1:1):(a.lvl!=b.lvl?(a.lvl<b.lvl?-1:1):0));
    }


    public void addSkill(EntityMob entity)
    {
        if(entity.getEntityWorld().isRemote)
            return;
        List<String> banList=AMConfig.getStringArray("am.monster."+ EntityList.getEntityString(entity)+".ban",new ArrayList<>());
        {
            double prob=AMConfig.getDouble("am.skill.prob",0.3f);
            double factor=AMConfig.getDouble("am.skill.factor",0.5f);
            double sumWeight=0;
            MobSkillData data = MobSkillData.get(entity);
            data.catalog  = Catalog.values()[RandUtils.nextInt(Catalog.values().length)];
            final boolean isTest=false;

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
                            if (!banList.contains(info.template.getId()))
                            {
                                if (info.lvl == level)
                                {
                                    filtList.add(info);
                                    sumWeight += info.prob;
                                } else
                                    break;
                            }
                        }
                    }//select available skill list
                    if (filtList.isEmpty())
                    {
                        if (mark >= list.size())
                        {
                            break;
                        }
                        else
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
                    last = info.lvl;
                    if (info.lvl == level)
                        level++;

                    float randExp = RandUtils.nextFloat();
                    randExp = 0.01f + randExp * randExp;
                    data.getSkillData().add(info.template, randExp);
                    filtList.clear();
                    sumWeight=0;
                }
                data.level = level;
            }
            else
            {
                data.getSkillData().add(AMElectronBomb.Instance, 0.732);
                data.getSkillData().add(AMElectronMissile.Instance, 0.732);
                data.level=5;
                data.catalog=Catalog.meltdown;
            }
        }
    }

}
