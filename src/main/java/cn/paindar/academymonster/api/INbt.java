package cn.paindar.academymonster.api;

import net.minecraft.nbt.NBTTagCompound;

public interface INbt {
    public void toNBT(NBTTagCompound tag);
    public void fromNBT(NBTTagCompound tag);
}
