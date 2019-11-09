package cn.paindar.academymonster.entity;

import cn.academy.entity.IRay;
import cn.lambdalib2.util.GameTimer;
import cn.lambdalib2.util.RandUtils;
import cn.lambdalib2.util.entityx.EntityAdvanced;
import cn.lambdalib2.util.entityx.EntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityRayBaseNative extends EntityAdvanced implements IRay
{

    EntityLivingBase spawner;

    public int life = 30;

    public double blendInTime = 0.1;

    public double blendOutTime = 0.3;
    public double widthShrinkTime = 0.3;

    public double length = 15.0;

    public double widthWiggleRadius = 0.1;
    public double maxWiggleSpeed = 0.4;
    public double widthWiggle = 0.0;

    public double glowWiggleRadius = 0.1;
    public double maxGlowWiggleSpeed = 0.4;
    public double glowWiggle = 0.0;

    public boolean viewOptimize = true;

    double lastFrame = 0;
    double creationTime;

    /**
     * This just link the ray to a player. You still have to setup the view direction based on the ray type.
     */
    public EntityRayBaseNative(EntityLivingBase player) {
        this(player.world);
        spawner = player;
    }

    public EntityRayBaseNative(World world) {
        super(world);
        creationTime = GameTimer.getTime();
        ignoreFrustumCheck = true;
    }

    public void setFromTo(Vec3d from, Vec3d to) {
        setFromTo(from.x, from.y, from.z, to.x, to.y, to.z);
    }

    public void setFromTo(double x0, double y0, double z0, double x1, double y1, double z1) {
        setPosition(x0, y0, z0);

        double dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        double dxzsq = dx * dx + dz * dz;
        rotationYaw = (float) (-Math.atan2(dx, dz) * 180 / Math.PI);
        rotationPitch = (float) (-Math.atan2(dy, Math.sqrt(dxzsq)) * 180 / Math.PI);

        length = Math.sqrt(dxzsq + dy * dy);
    }

    @Override
    protected void onFirstUpdate() {
        executeAfter(new EntityCallback() {
            @Override
            public void execute(Entity target) {
                setDead();
            }
        }, life);
    }

    protected double getDeltaTime() {
        return GameTimer.getTime() - creationTime;
    }

    @Override
    public double getLength() {
        double dt = getDeltaTime();
        return (dt < blendInTime ? dt / blendInTime : 1) * length;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        posX = tag.getDouble("x");
        posY = tag.getDouble("y");
        posZ = tag.getDouble("z");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setDouble("x", posX);
        tag.setDouble("y", posY);
        tag.setDouble("z", posZ);
    }

    public long getLifeMS() {
        return life * 50;
    }

    //TODO Add glow texture alpha wiggle
    @Override
    public double getAlpha() {
        double dt = getDeltaTime();
        double lifeMS = getLifeMS();
        return dt > lifeMS - blendOutTime ? 1 -  (dt + blendOutTime - lifeMS) / blendOutTime : 1.0;
    }

    @Override
    public double getWidth() {
        double dt = getDeltaTime();
        double lifeMS = getLifeMS();
        return widthWiggle +
                (dt > lifeMS - widthShrinkTime ? 1 - (dt + widthShrinkTime - lifeMS) / widthShrinkTime : 1.0);
    }

    @Override
    public boolean needsViewOptimize() {
        return viewOptimize;
    }

    @Override
    public double getStartFix() {
        return 0.0;
    }

    @Override
    public void onRenderTick() {
        double time = GameTimer.getTime();
        if(lastFrame != 0) {
            double dt = time - lastFrame;
            widthWiggle += dt * RandUtils.ranged(-maxWiggleSpeed, maxWiggleSpeed);
            if(widthWiggle > widthWiggleRadius)
                widthWiggle = widthWiggleRadius;
            if(widthWiggle < 0)
                widthWiggle = 0;

            glowWiggle += dt * RandUtils.ranged(-maxGlowWiggleSpeed, maxGlowWiggleSpeed) ;
            if(glowWiggle > glowWiggleRadius)
                glowWiggle = glowWiggleRadius;
            if(glowWiggle < 0)
                glowWiggle = 0;
        }

        lastFrame = GameTimer.getTime();
    }

    @Override
    public Vec3d getRayPosition() {
        return getPositionVector();
    }

    @Override
    public double getGlowAlpha() {
        double dt = getDeltaTime();
        double lifeMS = getLifeMS();
        return (1 - glowWiggleRadius + glowWiggle) * getAlpha();
    }

    @Override
    public EntityPlayer getPlayer() {
        return null;
    }
}
