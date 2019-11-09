package cn.paindar.academymonster.entity;

import cn.academy.Resources;
import cn.lambdalib2.registry.mc.RegEntity;
import cn.lambdalib2.registry.mc.RegEntityRender;
import cn.lambdalib2.util.GameTimer;
import cn.lambdalib2.util.RenderUtils;
import cn.lambdalib2.util.entityx.EntityAdvanced;
import cn.lambdalib2.util.entityx.MotionHandler;
import cn.lambdalib2.util.entityx.handlers.Rigidbody;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * Created by Paindar on 2017/2/12.
 */
@RegEntity
public class EntityMobCoinThrowing extends EntityAdvanced  implements IEntityAdditionalSpawnData
{

    private static final int MAXLIFE = 120;
    private static final double INITVEL = 0.92;

    private float initHt;
    private double maxHt;

    public EntityMob speller;

    public Vec3d axis;
    public boolean isSync = false;
    public EntityMobCoinThrowing(World world)
    {
        super(world);
    }

    public EntityMobCoinThrowing(EntityMob speller)
    {
        super(speller.world);
        this.speller = speller;
        this.initHt = (float) speller.posY;
        setPosition(speller.posX, speller.posY, speller.posZ);
        this.motionY = speller.motionY;
        setup();
        this.ignoreFrustumCheck = true;
    }

    private void setup() {
        Rigidbody rb = new Rigidbody();
        rb.gravity = 0.06;
        this.addMotionHandler(rb);
        this.addMotionHandler(new EntityMobCoinThrowing.KeepPosition());
        this.motionY += INITVEL;
        axis = new Vec3d(.1 + rand.nextDouble(), rand.nextDouble(), rand.nextDouble());
        this.setSize(0.2F, 0.2F);
    }

    private class KeepPosition extends MotionHandler<EntityMobCoinThrowing>
    {

        public KeepPosition() {}

        @Override
        public void onUpdate() {
            if(EntityMobCoinThrowing.this.speller != null) {
                posX = speller.posX;
                posZ = speller.posZ;
                if((posY < speller.posY && motionY < 0) || ticksExisted > MAXLIFE) {
                    finishThrowing();
                }
            }

            maxHt = Math.max(maxHt, posY);
        }

        @Override
        public String getID() {
            return "kip";
        }

        @Override
        public void onStart() {}

    }

    void finishThrowing() {
        setDead();
    }

    public double getProgress() {
        if(motionY > 0) { //Throwing up
            return (INITVEL - motionY) / INITVEL * 0.5;
        } else {
            return Math.min(1.0, 0.5 + ((maxHt - posY) / (maxHt - initHt)) * 0.5);
        }
    }

    @Override
    public void entityInit() {
    }


    @Override
    protected void readEntityFromNBT(NBTTagCompound p_70037_1_) {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound p_70014_1_) {

    }
    @Override
    public void writeSpawnData(ByteBuf buf)
    {
        NBTTagCompound nbt=new NBTTagCompound();
        if(speller!=null)
            nbt.setInteger("id",speller.getEntityId());
        if(axis!=null)
        {
            nbt.setDouble("xAxis", axis.x);
            nbt.setDouble("yAxis", axis.y);
            nbt.setDouble("zAxis", axis.z);
        }
        ByteBufUtils.writeTag(buf, nbt);
    }


    @Override
    public void readSpawnData(ByteBuf buf)
    {
        NBTTagCompound nbt= ByteBufUtils.readTag(buf);
        speller=(EntityMob) world.getEntityByID(nbt.getInteger("id"));
        axis=new Vec3d(nbt.getDouble("xAxis"),nbt.getDouble("yAxis"),nbt.getDouble("zAxis"));
        if(speller==null)
            setDead();
    }

    @SideOnly(Side.CLIENT)
    @RegEntityRender(EntityMobCoinThrowing.class)
    public static class R extends Render
    {

        public R(RenderManager manager) {
            super(manager);
        }

        @Override
        public void doRender(Entity var1, double x, double y, double z,
                             float var8, float var9) {
            EntityMobCoinThrowing etc = (EntityMobCoinThrowing) var1;
            EntityLivingBase player = etc.speller;
            double dt = GameTimer.getTime();
            if(player == null)
                return;
            //If syncedSingle and in client computer, do not render
            if(etc.posY < player.posY)
                return;
            GL11.glPushMatrix(); {
                //x = player.posX - RenderManager.renderPosX;
                //y = etc.posY - RenderManager.renderPosY;
                //z = player.posZ - RenderManager.renderPosZ;


                GL11.glTranslated(x, y, z);
                GL11.glRotated(player.renderYawOffset, 0, -1, 0);
                GL11.glTranslated(-0.63, -0.60, 0.30);
                float scale = 0.3F;
                GL11.glScalef(scale, scale, scale);
                GL11.glTranslated(0.5, 0.5, 0);
                GL11.glRotated((dt * 360.0 / 300.0), etc.axis.x, etc.axis.y, etc.axis.z);
                GL11.glTranslated(-0.5, -0.5, 0);
                RenderUtils.drawEquippedItem(0.0625, Resources.TEX_COIN_FRONT, Resources.TEX_COIN_BACK);
            } GL11.glPopMatrix();
        }

        @Override
        protected ResourceLocation getEntityTexture(Entity var1) {
            return null;
        }

    }



}
