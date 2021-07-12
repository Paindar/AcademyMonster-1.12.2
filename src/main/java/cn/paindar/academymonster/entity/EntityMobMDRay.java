package cn.paindar.academymonster.entity;

import cn.academy.client.render.entity.ray.RendererRayComposite;
import cn.academy.client.render.particle.MdParticleFactory;
import cn.academy.client.sound.ACSounds;
import cn.lambdalib2.particle.Particle;
import cn.lambdalib2.registry.mc.RegEntityRender;
import cn.lambdalib2.util.Colors;
import cn.lambdalib2.util.MathUtils;
import cn.lambdalib2.util.RandUtils;
import cn.lambdalib2.util.VecUtils;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Paindar on 2017/3/13.
 */
@SideOnly(Side.CLIENT)
public class EntityMobMDRay extends EntityRayBaseNative
{
    public EntityMobMDRay(Entity spawner, Vec3d str, Vec3d end) {
        super(spawner);
        this.setFromTo(str, end);
        this.blendInTime = 0.2;
        this.blendOutTime = 0.7;
        this.life = 14;
    }

    @Override
    protected void onFirstUpdate() {
        super.onFirstUpdate();
        ACSounds.playClient(world,posX, posY, posZ, "md.ray_small", SoundCategory.HOSTILE, 0.8f,1.0f);
    }

    @Override
    public double getWidth() {
        double dt = getDeltaTime();
        double blendTime = 0.5;

        if(dt > this.life * 0.05 - blendTime) {
            double timeFactor = MathUtils.clampd(0, 1, (double) (dt - (this.life * 0.05 - blendTime)) / blendTime);
            return 1 - timeFactor;
        }

        return 1.0;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        Particle p = MdParticleFactory.INSTANCE.next(world,
//                    new Motion3D(this, true).move(RandUtils.ranged(0, 10)).getPosVec(),
                VecUtils.lookingPos(this, RandUtils.ranged(0, 10)),
                new Vec3d(RandUtils.ranged(-.03, .03), RandUtils.ranged(-.03, .03), RandUtils.ranged(-.03, .03)));
        world.spawnEntity(p);
    }

    @SideOnly(Side.CLIENT)
    @RegEntityRender(EntityMobMDRay.class)
    public static class MDRayRender extends RendererRayComposite
    {

        public MDRayRender(RenderManager manager) {
            super(manager, "mdray_small");
            this.cylinderIn.width = 0.03;
            this.cylinderIn.color.set(216, 248, 216, 230);

            this.cylinderOut.width = 0.045;
            this.cylinderOut.color.set(106, 242, 106, 50);

            this.glow.width = 0.3;
            this.glow.color.setAlpha(Colors.f2i(0.5f));
        }

    }
}
