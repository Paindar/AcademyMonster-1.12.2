package cn.paindar.academymonster.ability;

import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class SkillTemplate {
    static final int WAITING=2333333;
    private final String nativeName;
    protected SkillTemplate(String i18n)
    {
        nativeName = i18n;
    }
    public String getId(){return nativeName;}
    public String getUnlocalizedSkillName(){return "am.ability." + nativeName + ".name";}
    public String getSkillName(){return I18n.translateToLocal(getUnlocalizedSkillName());}

    public abstract MonsterSkillInstance create(Entity e);
    public abstract SpellingInfo fromBytes(ByteBuf buf);
}
