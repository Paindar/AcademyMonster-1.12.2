package cn.paindar.academymonster.entity.datapart;

import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.paindar.academymonster.ability.SkillTemplate;
import cn.paindar.academymonster.ability.api.SkillInfo;
import cn.paindar.academymonster.ability.api.SkillRuntime;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import cn.paindar.academymonster.api.INbt;
import cn.paindar.academymonster.core.support.tile.AbilityInterfManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.*;


public class MonsterSkillList implements INbt, IMessage {
    private final List<SkillInfo> skillInfos = new ArrayList<>();
    private final List<SkillRuntime> skillRuntimes = new ArrayList<>();

    public SkillTemplate[] getSkills()
    {
        return skillInfos.stream().map(SkillInfo::getTemplate).toArray(SkillTemplate[]::new);
    }

    public void add(SkillTemplate template, double exp)
    {
        skillInfos.add(new SkillInfo(template, exp));

    }
    public void clear()
    {
        for(SkillRuntime rtm:skillRuntimes)
        {
            rtm.instance.clear();
        }
    }
    public double getSkillExp(String id)
    {
        for(SkillInfo info :skillInfos)
        {
            if(info.getTemplate().getId().equals(id))
                return info.getExp();
        }
        return 0;
    }
    public double getSkillExp(SkillTemplate template){return getSkillExp(template.getId());}

    public void init()
    {
    }
    protected SkillInfo findSkillInfo(SkillTemplate template)
    {
        for(SkillInfo info :skillInfos)
        {
            if(info.getTemplate().getId().equals(template.getId()))
            {
                return info;
            }
        }
        return null;
    }
    public int getCooldown(SkillTemplate template)
    {
        SkillInfo info = findSkillInfo(template);
        if(info!=null)
            return info.cooldown;
        return 0;
    }
    public void setCooldown(SkillTemplate template, int ticks)
    {
        SkillInfo info = findSkillInfo(template);
        if(info!=null)
            info.setCooldown(ticks);
        else
            throw new IllegalArgumentException("This entity doesn't have this skill!");
    }
    public MonsterSkillInstance execute(SkillTemplate template, Entity e)
    {
        MonsterSkillInstance ret = template.create(e);
        int cd = ret.execute();
        setCooldown(template, cd);
        skillRuntimes.add(new SkillRuntime(ret));
        return ret;
    }
    public void addCooldown(SkillTemplate template, int ticks)
    {
        SkillInfo info = findSkillInfo(template);
        if(info!=null)
            info.addCooldown(ticks);
        else
            throw new IllegalArgumentException("This entity doesn't have this skill!");
    }

    public void tick(boolean isInterfered) {
        if(!isInterfered)
        {
            for(SkillInfo info: skillInfos)
            {
                if(info.cooldown >0)
                    info.cooldown-=1;
            }
        }
        else
        {
            for(SkillInfo info: skillInfos)
            {
                info.addCooldown(AbilityInterfManager.MaxTick);
            }
        }
        skillRuntimes.removeIf((SkillRuntime r)->r.instance.isDisposed());
        for(SkillRuntime runtime : skillRuntimes)
        {
            runtime.tick();
        }
    }

    @Override
    public void toNBT(NBTTagCompound nbt) {
        nbt.setInteger("size", skillInfos.size());
        NBTTagList list = new NBTTagList();
        for(SkillInfo info : skillInfos)
        {
            NBTTagCompound tag = new NBTTagCompound();
            info.toNBT(tag);
            list.appendTag(tag);
        }
        nbt.setTag("skills", list);
    }

    @Override
    public void fromNBT(NBTTagCompound tag) {
        int size = tag.getInteger("size");
        NBTTagList list = (NBTTagList) tag.getTag("skills");
        clear();
        for(int i = 0;i<size;i++)
        {
            NBTTagCompound subTag = list.getCompoundTagAt(i);
            SkillInfo info = new SkillInfo(subTag);
            skillInfos.add(info);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = NetworkS11n.deserialize(buf);
        skillInfos.clear();
        for(int i=0;i<size;i++)
        {
            SkillInfo info = new SkillInfo();
            info.fromBytes(buf);
            skillInfos.add(info);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkS11n.serialize(buf, skillInfos.size(), false);
        for(SkillInfo info : skillInfos)
        {
            info.toBytes(buf);
        }
    }
}
