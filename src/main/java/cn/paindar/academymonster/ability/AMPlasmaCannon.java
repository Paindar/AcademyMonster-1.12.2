package cn.paindar.academymonster.ability;

import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.lambdalib2.s11n.network.TargetPoints;
import cn.lambdalib2.util.BlockSelectors;
import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.Raytrace;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import cn.paindar.academymonster.config.AMConfig;
import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.entity.EntityPlasmaBodyEffect;
import cn.paindar.academymonster.entity.EntityTornadoEffect;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import cn.paindar.academymonster.network.NetworkManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static cn.lambdalib2.util.MathUtils.lerp;

public class AMPlasmaCannon extends SkillTemplate
{
    public static final AMPlasmaCannon Instance = new AMPlasmaCannon();
    public static boolean canDestroyBlock = true;
    @StateEventCallback
    public static void postInit(FMLPostInitializationEvent evt)
    {
        canDestroyBlock = AMConfig.getBoolean("am.skill.plasma_cannon.destroyBlock", true);
    }
    protected AMPlasmaCannon() {
        super("plasma_cannon");
    }

    @Override
    public MonsterSkillInstance create(Entity e) {
        return new PlasmaCannonContext(e);
    }

    @Override
    public SpellingInfo fromBytes(ByteBuf buf) {
        SpellingInfo info = new AMPlasmaCannonClientInfo();
        info.fromBytes(buf);
        return info;
    }

    public static class PlasmaCannonContext extends MonsterSkillInstance
    {
        public double time;
        public EntityPlasmaBodyEffect effect;
        public EntityTornadoEffect body;
        private final int cooldown;

        public PlasmaCannonContext(Entity ent) {
            super(AMPlasmaCannon.Instance, ent);
            cooldown = (int) lerp(400,650,getExp());
        }

        @Override
        public int execute() {
            effect = new EntityPlasmaBodyEffect(speller, this);
            time=0;
            effect.setPosition(speller.posX, speller.posY + 15, speller.posZ);
            speller.world.spawnEntity(effect);
            body=new EntityTornadoEffect(speller.world, speller);
            speller.world.spawnEntity(body);
            return WAITING;
        }
        @Override
        public void tick()
        {
            time++;
            if(time>120)
            {
                Vec3d result = Raytrace.getLookingPos(speller, 10, EntitySelectors.nothing(), BlockSelectors.filEverything).getKey();
                flyTo(result.x, result.y, result.z);
                clear();
            }
            else if(speller.isDead || effect==null||effect.isDead) {
                clear();
            }
        }
        private void flyTo(double x, double y, double z)
        {
            effect.setTargetPoint(x,y,z);
            AMPlasmaCannonClientInfo info = new AMPlasmaCannonClientInfo();
            info.e = body;
            NetworkManager.sendSkillEventAllAround(TargetPoints.convert(effect, 20), speller, AMPlasmaCannon.Instance, info);
        }

        public void clear() {
            if(effect != null)
            {
                AMPlasmaCannonClientInfo info = new AMPlasmaCannonClientInfo();
                info.e = effect;
                NetworkManager.sendSkillEventAllAround(TargetPoints.convert(effect, 20), speller, AMPlasmaCannon.Instance, info);
            }
            MobSkillData.get((EntityMob) speller).getSkillData().setCooldown(template, cooldown);
            setDisposed();
        }
    }

    public static class AMPlasmaCannonClientInfo extends SpellingInfo
    {
        public Entity e;
        @Override
        public void fromBytes(ByteBuf buf) {
            e = NetworkS11n.deserialize(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            NetworkS11n.serialize(buf, e, false);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void action(Entity speller) {
            if(e instanceof EntityPlasmaBodyEffect)
                ((EntityPlasmaBodyEffect)e).changeState();
            else if(e instanceof EntityTornadoEffect)
                ((EntityTornadoEffect)e).changeState();
        }
    }
}
