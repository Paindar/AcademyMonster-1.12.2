package cn.paindar.academymonster.ability;

import cn.academy.client.render.util.ACRenderingHelper;
import cn.academy.client.sound.ACSounds;
import cn.academy.entity.EntityBloodSplash;
import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.lambdalib2.s11n.network.TargetPoints;
import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.RandUtils;
import cn.lambdalib2.util.Raytrace;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.ability.client.EffectSpawner;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import cn.paindar.academymonster.network.NetworkManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static cn.lambdalib2.util.MathUtils.lerp;

public class AMBloodRetro extends SkillTemplate {
    public static final AMBloodRetro Instance = new AMBloodRetro();
    protected AMBloodRetro() {
        super("blood_retro");
    }

    @Override
    public MonsterSkillInstance create(Entity e) {
        return new BloodRetroContext(e);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpellingInfo fromBytes(ByteBuf buf) {
        SpellingInfo info = new AMBloodRetro.BloodRetroClientInfo();
        info.fromBytes(buf);
        return info;
    }
    static class BloodRetroContext extends MonsterSkillInstance
    {
        private final double damage;
        private final double range;
        private final int cooldown;
        public BloodRetroContext(Entity ent) {
            super(AMBloodRetro.Instance, ent);
            damage=lerp(7,14,getExp());
            range=lerp(1,4,getExp());
            cooldown = (int) lerp(60,30,getExp());
        }

        @Override
        public int execute() {
            RayTraceResult result= Raytrace.traceLiving(speller,range, EntitySelectors.living());
            EntityLivingBase target;
            if(result.typeOfHit==RayTraceResult.Type.ENTITY)
            {
                target=(EntityLivingBase)result.entityHit;
                attack(target,damage, true);
                target.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("poison"), (int)(200*getExp()), 2));
                BloodRetroClientInfo info = new BloodRetroClientInfo();
                info.target = target;
                NetworkManager.sendSkillEventAllAround(TargetPoints.convert(target, 12), speller,
                        Instance, info);
            }
            else if(result.typeOfHit == RayTraceResult.Type.MISS)
            {

                setDisposed();
                return 5;
            }
            setDisposed();
            return cooldown;
        }

        @Override
        public void clear() {

        }
    }

    static class BloodRetroClientInfo extends SpellingInfo
    {
        EntityLivingBase target;
        @Override
        @SideOnly(Side.CLIENT)
        public void action(Entity speller) {
            if(target==null)
                return;
            ACSounds.playClient(target, "tp.guts", SoundCategory.HOSTILE, 0.6f);
            for(int i = 0; i< RandUtils.rangei(4, 6); i++)
            {
                double y = target.posY + RandUtils.ranged(0, 1) * target.height;
                if(target instanceof EntityPlayer)
                    y += ACRenderingHelper.getHeightFix((EntityPlayer)target);

                double theta = RandUtils.ranged(0, Math.PI * 2);
                double r  = 0.5 * RandUtils.ranged(0.8 * target.width, target.width);
                EntityBloodSplash splash = new EntityBloodSplash(target.world);
                splash.setPosition(target.posX + r * Math.sin(theta), y, target.posZ + r * Math.cos(theta));
                //speller.world.spawnEntity(splash);
                EffectSpawner.Instance.addEffect(splash);
            }
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            try {
                target = NetworkS11n.deserialize(buf);
            }catch(Exception e)
            {
                target=null;
            }
        }

        @Override
        public void toBytes(ByteBuf buf) {
            NetworkS11n.serialize(buf, target, false);
        }
    }
}
