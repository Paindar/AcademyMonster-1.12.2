package cn.paindar.academymonster.ability.api;


import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class SpellingInfo implements IMessage {
    public abstract void action(Entity speller);
}
