package cn.paindar.academymonster.ability;

import cn.academy.client.render.util.ArcPatterns;
import cn.academy.client.sound.ACSounds;
import cn.lambdalib2.s11n.network.TargetPoints;
import cn.lambdalib2.util.*;
import cn.lambdalib2.util.entityx.handlers.Life;
import cn.paindar.academymonster.ability.api.SpellingInfo;
import cn.paindar.academymonster.ability.client.EffectSpawner;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import cn.paindar.academymonster.entity.EntityMobArc;
import cn.paindar.academymonster.network.NetworkManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static cn.lambdalib2.util.MathUtils.lerp;

public class AMArcGen extends SkillTemplate {
    public static final AMArcGen Instance = new AMArcGen();
    protected AMArcGen() {
        super("arc");
    }
    @Override
    public MonsterSkillInstance create(Entity e) {
        return new ArcGenContext(e);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpellingInfo fromBytes(ByteBuf buf) {
        SpellingInfo info = new ArcGenClientInfo();
        info.fromBytes(buf);
        return info;
    }
    static class ArcGenContext extends MonsterSkillInstance
    {
        private final double damage;
        private final double range ;
        private final double prob;//burning prob
        private final double slowdown;
        private final int cooldown;

        public ArcGenContext(Entity ent) {
            super(Instance, ent);
            damage=lerp(1.,7., getExp());
            range=lerp(6,15,getExp());
            prob=lerp(0,0.6f,getExp());
            slowdown= getExp()>0.5?lerp(0,0.8,getExp()-0.5):0;
            cooldown = (int)lerp(40,20,getExp());
        }

        @Override
        public int execute()
        {
            RayTraceResult result=Raytrace.traceLiving(speller, range, EntitySelectors.living(), BlockSelectors.filNormal);
            switch(result.typeOfHit)
            {
                case ENTITY:
                    if (result.entityHit instanceof EntityLivingBase)
                    {
                        EntityLivingBase target = (EntityLivingBase) result.entityHit;
                        attack(target, damage,false);
                        if (RandUtils.nextDouble() <= slowdown)
                        {
                            target.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), 10));
                        }
                    }
                    break;
            /*case BLOCK:
                if(getSkillExp()>=0.4)
                {
                    BlockPos pos=result.getBlockPos(),
                            abovePos = new BlockPos(pos.getX(), pos.getY()+1, pos.getZ());
                    if (RandUtils.ranged(0, 1) < prob)
                    {
                        if (world.getBlockState(abovePos).getBlock() == Blocks.AIR) {
                            world.setBlockState(abovePos, Blocks.FIRE.getDefaultState());
                        }
                    }
                }
                break;*/
            }
            ArcGenClientInfo info = new ArcGenClientInfo();
            info.range = range;
            NetworkManager.sendSkillEventAllAround(TargetPoints.convert(speller, 25), speller, Instance, info);
            setDisposed();
            return cooldown;
        }

        @Override
        public void clear() { }
    }

    static class ArcGenClientInfo extends SpellingInfo
    {
        double range;
        @Override
        public void fromBytes(ByteBuf buf) {
            range = buf.readDouble();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeDouble(range);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void action(Entity speller) {
            EntityMobArc arc = new EntityMobArc(speller, ArcPatterns.weakArc);
            arc.texWiggle = 0.7;
            arc.showWiggle = 0.1;
            arc.hideWiggle = 0.4;
            arc.addMotionHandler(new Life(10));
            arc.lengthFixed = false;
            arc.length = range;
            EffectSpawner.Instance.addEffect(arc);
            //speller.world.spawnEntity(arc);
            ACSounds.playClient(speller.world,speller.posX, speller.posY, speller.posZ, "em.arc_weak",
                    SoundCategory.HOSTILE, .5f, 1f);

        }
    }
}
