package cn.paindar.academymonster.ability;

import cn.academy.client.render.util.ArcPatterns;
import cn.academy.client.sound.ACSounds;
import cn.academy.client.sound.FollowEntitySound;
import cn.lambdalib2.s11n.network.NetworkS11n;
import cn.lambdalib2.s11n.network.TargetPoints;
import cn.lambdalib2.util.*;
import cn.lambdalib2.util.entityx.handlers.Life;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.ability.client.EffectSpawner;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import cn.paindar.academymonster.entity.EntityMobArc;
import cn.paindar.academymonster.entity.EntityRailgunFXNative;
import cn.paindar.academymonster.network.NetworkManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Predicate;

import static cn.lambdalib2.util.MathUtils.lerp;

public class AMThunderBolt extends SkillTemplate{
    public static final AMThunderBolt Instance = new AMThunderBolt();

    protected AMThunderBolt(){
        super("thunder_bolt");
    }

    @Override
    public MonsterSkillInstance create(Entity e) {
        return new ThunderBoltContext(e);
    }

    @Override
    public SpellingInfo fromBytes(ByteBuf buf) {
        ThunderBoltClientInfo ret = new ThunderBoltClientInfo();
        ret.fromBytes(buf);
        return ret;
    }

    static class ThunderBoltContext extends MonsterSkillInstance
    {
        private static final float RANGE=20f;
        private static final float AOE_RANGE=7f;
        private final double aoeDamage;
        private final double damage;
        private final int cooldown;
        public ThunderBoltContext(Entity ent) {
            super(Instance, ent);
            aoeDamage = lerp(9.6f, 17.4f, getExp());
            damage = lerp(16f, 29f, getExp());
            cooldown = (int)lerp(200,100,getExp());
        }

        @Override
        public int execute() {
            RayTraceResult result = Raytrace.traceLiving(speller, RANGE);
            Vec3d end;
            boolean hitEntity = false;
            switch(result.typeOfHit)
            {
                case BLOCK:
                    end = result.hitVec;
                    break;
                case ENTITY:
                    end = speller.getPositionEyes(1f);
                    hitEntity = true;
                    break;
                default:
                    end = VecUtils.lookingPos(speller, RANGE);
            }

            Predicate<Entity> exclusion= (!hitEntity)? EntitySelectors.exclude(speller) : EntitySelectors.exclude(speller, result.entityHit);
            EntityLivingBase target = (hitEntity)? (EntityLivingBase)result.entityHit : null;
            List<Entity> aoes = WorldUtils.getEntities(
                    speller.world, end.x, end.y, end.z,
                    AOE_RANGE, EntitySelectors.living().and(exclusion));

            if(target != null)
            {
                attack(target, damage,false );
                if(getExp() > 0.2 && RandUtils.ranged(0, 1) < 0.8 ) {
                    target.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), 40, 3));
                }
            }

            for(Entity e:aoes)
            {
                attack((EntityLivingBase) e, aoeDamage,false);

                if (getExp() > 0.2 && RandUtils.ranged(0, 1) < 0.8)
                {
                    ((EntityLivingBase)e).addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), 20, 3));
                }
            }
            ThunderBoltClientInfo info = new ThunderBoltClientInfo();
            info.aoes = aoes;
            info.end = end;
            NetworkManager.sendSkillEventAllAround(TargetPoints.convert(speller, 20),
                    speller, Instance, info);

            setDisposed();
            return cooldown;
        }

        @Override
        public void clear() {
        }
    }
    static class ThunderBoltClientInfo extends SpellingInfo
    {
        List<Entity> aoes;
        Vec3d end;
        @Override
        @SideOnly(Side.CLIENT)
        public void action(Entity speller) {
            for(int i= 0 ;i<2;i++)
            {
                EntityMobArc mainArc = new EntityMobArc(speller, ArcPatterns.strongArc);
                mainArc.length = ThunderBoltContext.RANGE;
                mainArc.addMotionHandler(new Life(20));
                //speller.world.spawnEntity(mainArc);
                EffectSpawner.Instance.addEffect(mainArc);
            }

            for(Entity e:aoes)
            {
                EntityMobArc aoeArc = new EntityMobArc(speller, ArcPatterns.aoeArc);
                aoeArc.lengthFixed = false;
                aoeArc.setFromTo(end.x, end.y,end.z,
                        e.posX, e.posY + e.getEyeHeight(), e.posZ);
                aoeArc.addMotionHandler(new Life(RandUtils.rangei(15, 25)));
                //speller.world.spawnEntity(aoeArc);
                EffectSpawner.Instance.addEffect(aoeArc);
            }

            ACSounds.playClient(speller.world, speller.posX, speller.posY, speller.posZ,
                    "em.arc_strong", SoundCategory.HOSTILE, 0.6f, 1f);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            aoes = NetworkS11n.deserialize(buf);
            end = NetworkS11n.deserialize(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            NetworkS11n.serialize(buf, aoes, false);
            NetworkS11n.serialize(buf, end,false);
        }
    }
}
