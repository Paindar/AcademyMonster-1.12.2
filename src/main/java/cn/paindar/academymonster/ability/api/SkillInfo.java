package cn.paindar.academymonster.ability.api;

import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.paindar.academymonster.ability.SkillTemplate;
import cn.paindar.academymonster.api.INbt;
import cn.paindar.academymonster.core.SkillManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SkillInfo implements INbt, IMessage
{
    protected SkillTemplate template;
    public int cooldown=0;
    protected double exp;
    public SkillInfo(){}
    public SkillInfo(NBTTagCompound tag){this.fromNBT(tag);};
    public SkillInfo(SkillTemplate template, double exp) {
        this.template = template;
        this.exp = exp;
    }

    public boolean canUse(){return cooldown ==0;}
    public SkillTemplate getTemplate(){return template;}
    public double getExp(){return exp;}
    public void addCooldown(int newCooldown){cooldown += newCooldown;}
    public void setCooldown(int newCooldown){cooldown = newCooldown;}


    @Override
    public void toNBT(NBTTagCompound tag) {
        tag.setString("skill", template.getId());
        tag.setInteger("cd", cooldown);
        tag.setDouble("exp", exp);
    }

    @Override
    public void fromNBT(NBTTagCompound tag) {
        String skill_name = tag.getString("skill");
        template = SkillManager.instance.getSkillTemplate(skill_name);
        cooldown = tag.getInteger("cd");
        exp = tag.getDouble("exp");
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        String skill_name = NetworkS11n.deserialize(buf);
        template = SkillManager.instance.getSkillTemplate(skill_name);
        cooldown = NetworkS11n.deserialize(buf);
        exp = NetworkS11n.deserialize(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkS11n.serialize(buf, template.getId(), false);
        NetworkS11n.serialize(buf, cooldown, false);
        NetworkS11n.serialize(buf, exp, false);
    }
}
