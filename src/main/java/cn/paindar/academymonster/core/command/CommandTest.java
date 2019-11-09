package cn.paindar.academymonster.core.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.List;

/**
 * Created by Paindar on 2017/2/13.
 */
public class CommandTest extends CommandBase
{
    @Override
    public String getName()
    {
        return "amtest";
    }

    @Override
    public String getUsage(ICommandSender p_71518_1_)
    {
        return "commands.position.usage";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
//        EntityPlayer player = (EntityPlayer) sender;
//        List<Entity> list = WorldUtils.getEntities(player,40, EntitySelectors.everything());
//        for(Entity e:list){
//            if(e instanceof EntityLivingBase)
//            {
//                AcademyMonster.log.info(String.format("EntityName=%s, health=%f/%f, have skill: %s",
//                        e.getCommandSenderName(),((EntityLivingBase)e).getHealth(),((EntityLivingBase) e).getMaxHealth(),
//                        MobSkillData.get(e).getSkillData()));
//            }
//        }
//        if (args.length > 1)
//        {
//            throw new WrongUsageException("commands.position.usage");
//        }
//        else
//        {
//
//        }
    }

}
