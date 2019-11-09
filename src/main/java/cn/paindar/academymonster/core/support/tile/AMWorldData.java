package cn.paindar.academymonster.core.support.tile;

import cn.paindar.academymonster.core.AcademyMonster;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Paindar on 2017/3/23.
 */
public class AMWorldData extends WorldSavedData
{
    private static String TAG= AcademyMonster.MODID;
    Set<BlockPos> set=new LinkedHashSet<>();
    public static AMWorldData get(World world)
    {
        WorldSavedData data = world.getPerWorldStorage().getOrLoadData(AMWorldData.class, TAG);
        if (data == null)
        {
            data = new AMWorldData(TAG);
            world.getPerWorldStorage().setData(TAG, data);
        }
        return (AMWorldData) data;
    }

    public AMWorldData(String name)
    {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        int size=nbt.getInteger("AMtAIM_size");
        for(int i=0;i<size;i++)
        {
            int[] pos=nbt.getIntArray(String.valueOf(i));
            set.add(new BlockPos(pos[0], pos[1], pos[2]));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        int i=0;
        for(BlockPos value:set)
        {
            nbt.setIntArray(String.valueOf(i++), new int[]{value.getX(), value.getY(), value.getZ()});
        }
        nbt.setInteger("AMtAIM_size",i);
        return nbt;
    }
}
