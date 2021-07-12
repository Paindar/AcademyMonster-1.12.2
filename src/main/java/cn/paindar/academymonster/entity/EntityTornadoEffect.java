package cn.paindar.academymonster.entity;

import cn.academy.ability.vanilla.vecmanip.client.effect.TornadoEffect;
import cn.academy.ability.vanilla.vecmanip.client.effect.TornadoRenderer;
import cn.lambdalib2.registry.mc.RegEntity;
import cn.lambdalib2.registry.mc.RegEntityRender;
import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.Raytrace;
import cn.lambdalib2.util.VecUtils;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static org.lwjgl.opengl.GL11.*;

@RegEntity
public class EntityTornadoEffect extends Entity {

    TornadoEffect theTornado = new TornadoEffect(12, 8, 1, 0.3);

    boolean dead = false;
    int deadTick = 0;
    Entity mob;
    boolean state = false;

    public EntityTornadoEffect(World worldIn) {
        super(worldIn);
    }

    public EntityTornadoEffect(World world, Entity mob) {
        this(world);
        this.mob = mob;
        Vec3d initPos = null;
        Vec3d p0 = mob.getPositionVector();
        Vec3d p1 = VecUtils.add(p0, new Vec3d(0.0, -20.0, 0.0));
        RayTraceResult result = Raytrace.perform(mob.world, p0, p1, EntitySelectors.nothing());
        if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            initPos = result.hitVec;
        } else {
            initPos = p1;
        }
        this.setPosition(initPos.x, initPos.y + 15, initPos.z);
        ignoreFrustumCheck = true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (state) {
            dead = true;
        }
        if (!world.isRemote && (mob == null || mob.isDead)) {
            setDead();
            dead = true;
        }

        if (dead) {
            deadTick += 1;
            if (deadTick == 30) {
                setDead();
            }
        }
        theTornado.alpha_$eq(alpha() * 0.5f);
    }

    float alpha() {
        if (!dead) {
            if (ticksExisted < 20.0f)
                return ticksExisted / 20.0f;
            else
                return 1.0f;
        } else {
            return 1 - deadTick / 20.0f;
        }
    }

    public void changeState() {
        state = !state;
    }

    @Override
    public boolean shouldRenderInPass(int pass)
    {
        return pass == 1;
    }

    @Override
    protected void entityInit() {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        setDead();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {

    }

    @SideOnly(Side.CLIENT)
    @RegEntityRender(EntityTornadoEffect.class)
    public static class TornadoEntityRenderer extends Render<EntityTornadoEffect> {

        public TornadoEntityRenderer(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityTornadoEffect entity, double x, double y, double z, float v3, float v4)
        {
            glPushMatrix();
            glTranslated(x, y, z);

            glDisable(GL_ALPHA_TEST);
            TornadoRenderer.doRender(entity.theTornado);

            glPopMatrix();
        }

        @Override
        public ResourceLocation getEntityTexture(EntityTornadoEffect entity)
        {
            return null;
        }
    }
}
