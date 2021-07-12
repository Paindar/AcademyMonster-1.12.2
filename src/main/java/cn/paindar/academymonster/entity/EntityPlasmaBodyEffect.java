package cn.paindar.academymonster.entity;

import cn.academy.client.CameraPosition;
import cn.lambdalib2.registry.mc.RegEntity;
import cn.lambdalib2.registry.mc.RegEntityRender;
import cn.lambdalib2.render.legacy.GLSLMesh;
import cn.lambdalib2.render.legacy.LegacyMeshUtils;
import cn.lambdalib2.render.legacy.LegacyShaderProgram;
import cn.lambdalib2.s11n.network.TargetPoints;
import cn.lambdalib2.util.*;
import cn.paindar.academymonster.ability.AMPlasmaCannon;
import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.network.NetworkManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static cn.lambdalib2.util.MathUtils.lerpf;
import static cn.lambdalib2.util.RandUtils.rangef;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;


@RegEntity
public class EntityPlasmaBodyEffect extends Entity {

    boolean state = false;
    boolean flying = false;
    Entity mob;
    Vec3d delta;
    double initTime = GameTimer.getTime();
    float alpha = 0.0f;
    private AMPlasmaCannon.PlasmaCannonContext skill = null;

    static class TrigPar {
        public float amp;
        public float speed;
        public float dphase;

        public TrigPar(float amp, float speed, float dphase) {
            this.amp = amp;
            this.speed = speed;
            this.dphase = dphase;
        }

        public float phase(float time) {
            return speed * time - dphase;
        }
    }

    static class BallInst {
        float size;
        Vec3d center;
        TrigPar hmove;
        TrigPar vmove;

        public BallInst(float size, Vec3d center, TrigPar hmove, TrigPar vmove) {
            this.size = size;
            this.center = center;
            this.hmove = hmove;
            this.vmove = vmove;
        }
    }

    List<BallInst> balls = new ArrayList<>();

    public EntityPlasmaBodyEffect(World worldIn) {
        super(worldIn);
        for (int i = 0; i < 4; i++) {
            float rvf = rangef(-1.5f, 1.5f);
            balls.add(new BallInst(rangef(1, 1.5f),
                    new Vec3d(rvf, rvf, rvf),
                    nextTrigPar(),
                    nextTrigPar()));
        }
        int limit = RandUtils.nextInt(2) + 4;
        for (int i = 0; i < limit; i++) {
            float rvf = rangef(-3f, 3f);
            balls.add(new BallInst(rangef(0.1f, 0.3f),
                    new Vec3d(rvf, rvf, rvf),
                    nextTrigPar(2.5f),
                    nextTrigPar(2.5f)));
        }
        setSize(10, 10);
        ignoreFrustumCheck = true;
    }

    public EntityPlasmaBodyEffect(Entity mob, AMPlasmaCannon.PlasmaCannonContext skill) {
        this(mob.world);
        this.mob = mob;
        this.skill = skill;
        //def rvf = rangef(-1.5f, 1.5f)
        for (int i = 0; i < 4; i++) {
            float rvf = rangef(-1.5f, 1.5f);
            balls.add(new BallInst(rangef(1, 1.5f),
                    new Vec3d(rvf, rvf, rvf),
                    nextTrigPar(),
                    nextTrigPar()));
        }
        int limit = RandUtils.nextInt(2) + 4;
        for (int i = 0; i < limit; i++) {
            float rvf = rangef(-3f, 3f);
            balls.add(new BallInst(rangef(0.1f, 0.3f),
                    new Vec3d(rvf, rvf, rvf),
                    nextTrigPar(2.5f),
                    nextTrigPar(2.5f)));
        }
        setSize(10, 10);
        ignoreFrustumCheck = true;
    }

    public TrigPar nextTrigPar() {
        return nextTrigPar(1f);
    }

    public TrigPar nextTrigPar(float size) {
        float amp = rangef(1.4f, 2f) * size;
        float speed = rangef(0.5f, 0.7f);
        float dphase = rangef(0, MathUtils.PI_F * 2);
        return new TrigPar(amp, speed, dphase);
    }

    public double deltaTime() {
        return (GameTimer.getTime() - initTime);
    }

    private void explode() {
        WorldUtils.getEntities(world, posX, posY, posZ, 10, EntitySelectors.living())
                .forEach((e) -> {
                    if (e instanceof EntityLivingBase) {
                        if (e != mob) {
                            skill.attack((EntityLivingBase) e, rangef(0.8f, 1.2f) * lerpf(20, 45, (float) skill.getExp()),false);
                            e.hurtResistantTime = -1;
                        }
                    }
                });

        Explosion explosion = new Explosion(world, this,
                posX, posY, posZ,
                lerpf(12.0f, 15.0f, (float) skill.getExp()), false, true);

        if (AMPlasmaCannon.canDestroyBlock) {
            explosion.doExplosionA();
        }
        explosion.doExplosionB(true);
        flying = false;
        setDead();
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote && mob.isDead) {
            setDead();
            return;
        }
        boolean terminated = state;
        if (flying) {
            Vec3d start = getPositionVector();
            posX += delta.x;
            posY += delta.y;
            posZ += delta.z;
            RayTraceResult result = Raytrace.perform(world, start, VecUtils.add(start, VecUtils.multiply(delta, 3.0)),
                    EntitySelectors.living(), BlockSelectors.filNormal);
            if (result.typeOfHit != RayTraceResult.Type.MISS && !world.isRemote) {
                explode();
                AMPlasmaCannon.AMPlasmaCannonClientInfo info = new AMPlasmaCannon.AMPlasmaCannonClientInfo();
                info.e = this;
                NetworkManager.sendSkillEventAllAround(TargetPoints.convert(this, 20), mob, AMPlasmaCannon.Instance, info);
            }
        }
        if (terminated && Math.abs(alpha) <= 1e-3f) {
            setDead();
        }
    }

    public void updateAlpha() {
        double dt = deltaTime();
        boolean terminated = state;
        int desiredAlpha = terminated ? 0 : 1;

        alpha = move(alpha, desiredAlpha, (float) (dt * (terminated ? 1f : 0.3f)));
        initTime = GameTimer.getTime();
    }

    public void changeState() {
        state = !state;
    }

    private float move(float from, float to, float max) {
        float delta = to - from;
        return from + Math.min(Math.abs(delta), max) * Math.signum(delta);
    }

    public void setTargetPoint(double x, double y, double z) {
        delta = new Vec3d(x - posX, y - posY, z - posZ).normalize();
        flying = true;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
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
    @RegEntityRender(EntityPlasmaBodyEffect.class)
    public static class PlasmaBodyRenderer extends Render {
        GLSLMesh mesh = LegacyMeshUtils.createBillboard(new GLSLMesh(), -.5, -.5, .5, .5);

        LegacyShaderProgram shader = new LegacyShaderProgram();


        int pos_ballCount;
        int  pos_balls;
        int pos_alpha;

        public PlasmaBodyRenderer(RenderManager renderManager) {
            super(renderManager);
            shader.linkShader(new ResourceLocation("academy:shaders/plasma_body.vert"), GL_VERTEX_SHADER);
            shader.linkShader(new ResourceLocation("academy:shaders/plasma_body.frag"), GL_FRAGMENT_SHADER);
            shader.compile();
            pos_ballCount = shader.getUniformLocation("ballCount");
            pos_balls     = shader.getUniformLocation("balls");
            pos_alpha     = shader.getUniformLocation("alpha");
        }

        @Override
        public void doRender(Entity entity, double x, double y, double z, float partialTicks, float wtf) {
            if (entity instanceof EntityPlasmaBodyEffect) {
                EntityPlasmaBodyEffect eff = (EntityPlasmaBodyEffect) entity;
                int size = 22;

                Vector3f playerPos = new Vector3f(
                        (float)renderManager.viewerPosX,
                        (float)renderManager.viewerPosY,
                        (float)renderManager.viewerPosZ);

                Matrix4f matrix = new Matrix4f();
                acquireMatrix(GL_MODELVIEW_MATRIX, matrix);

                glDepthMask(false);
                glEnable(GL_BLEND);
                glDisable(GL_ALPHA_TEST);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glUseProgram(shader.getProgramID());

                // update ball location
                float deltaTime = (float) eff.deltaTime();

                eff.updateAlpha();

                float alpha = (float) Math.pow(eff.alpha, 2);

                glUniform1i(pos_ballCount, eff.balls.size());
                for(int idx=0;idx < eff.balls.size();idx++)
                {
                    BallInst ball = eff.balls.get(idx);
                    float hrphase = ball.hmove.phase(deltaTime);
                    float vtphase = ball.vmove.phase(deltaTime);

                    float dx = ball.hmove.amp * MathHelper.sin(hrphase);
                    float dy = ball.vmove.amp * MathHelper.sin(vtphase);
                    float dz = ball.hmove.amp * MathHelper.cos(hrphase);

                    Vector4f pos = new Vector4f(
                            (float)(eff.posX + ball.center.x + dx - playerPos.x),
                            (float)(eff.posY + ball.center.y + dy - playerPos.y),
                            (float)(eff.posZ + ball.center.z + dz - playerPos.z), 1);

                    Vector4f camPos = Matrix4f.transform(matrix, pos, null);
                    glUniform4f(pos_balls + idx, camPos.x, camPos.y, -camPos.z, ball.size);
                }


                glUniform1f(pos_alpha, alpha);
                //

                Vec3d campos = CameraPosition.getVec3d();

                Vec3d delta = new Vec3d(x, y, z).subtract(campos);
                EntityLook yp = new EntityLook(delta);

                glPushMatrix();

                glTranslated(x, y, z);
                glRotated(-yp.yaw + 180, 0, 1, 0);
                glRotated(-yp.pitch, 1, 0, 0);
                glScaled(size, size, 1);

                mesh.draw(shader.getProgramID());

                glPopMatrix();

                glUseProgram(0);
                glEnable(GL_ALPHA_TEST);
                glDepthMask(true);
            }
        }

        private void acquireMatrix ( int matrixType, Matrix4f dst){
            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            glGetFloat(matrixType, buffer);
            dst.load(buffer);
        }

        @Nullable
        @Override
        protected ResourceLocation getEntityTexture (Entity entity){
            return null;
        }
    }
}
