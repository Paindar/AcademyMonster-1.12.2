package cn.paindar.academymonster.entity;

import cn.lambdalib2.registry.mc.RegEntity;
import cn.lambdalib2.s11n.network.TargetPoints;
import cn.lambdalib2.util.RandUtils;
import cn.lambdalib2.util.VecUtils;
import cn.lambdalib2.util.entityx.event.CollideEvent;
import cn.lambdalib2.util.entityx.handlers.Rigidbody;
import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;
import cn.paindar.academymonster.core.AcademyMonster;
import cn.paindar.academymonster.network.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Created by Paindar on 2017/6/4.
 */
@RegEntity(
        freq = 10
)
public class EntityMagManipBlock extends EntityBlockNative
{
    private final static int ActNothing = 0, ActMoveTo = 1;
    private static final DataParameter<Integer> ACTION_TYPE;
    private static final DataParameter<Float> TX;
    private static final DataParameter<Float> TY;
    private static final DataParameter<Float> TZ;
    static {
        ACTION_TYPE = EntityDataManager.createKey(EntityMagManipBlock.class, DataSerializers.VARINT);
        TX = EntityDataManager.createKey(EntityMagManipBlock.class, DataSerializers.FLOAT);
        TY = EntityDataManager.createKey(EntityMagManipBlock.class, DataSerializers.FLOAT);
        TZ = EntityDataManager.createKey(EntityMagManipBlock.class, DataSerializers.FLOAT);
    }

    private float damage;

    private Entity player2;

    private float yawSpeed = RandUtils.rangef(1, 3);
    private float pitchSpeed = RandUtils.rangef(1, 3);

    private int actionType=1;
    private float tx;
    private float ty;
    private float tz;
    public double radium=0;

    private MonsterSkillInstance skill_Instance;
    public EntityMagManipBlock(World world)
    {
        super(world);
        placeWhenCollide=false;
    }

    public EntityMagManipBlock(Entity spawner, float damage, MonsterSkillInstance skill)
    {
        this(spawner.world);
        this.damage = damage;
        this.player2 = spawner;
        this.skill_Instance=skill;
    }

    public void setMoveTo(double x, double y, double z)
    {
        //这里可能发生坐标错位，原因同踏击导向等技能
        actionType = ActMoveTo;
        tx = (float)x;
        ty = (float)y;
        tz = (float)z;
    }

    @Override
    public void setPlaceFromServer(boolean value) {
        placeWhenCollide = value;

        NetworkManager.sendMagToAllAround(
                TargetPoints.convert(this, 20),
                this, value
        );
    }

    public void stopMoveTo()
    {
        actionType = ActNothing;
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        this.dataManager.register(ACTION_TYPE, 1);
        this.dataManager.register(TX, 0f);
        this.dataManager.register(TY, 0f);
        this.dataManager.register(TZ, 0f);
    }

    @Override
    public void onFirstUpdate()
    {
        super.onFirstUpdate();

        Rigidbody rb = getMotionHandler(Rigidbody.class);
        rb.entitySel = t -> t != player2;

        regEventHandler(new CollideEvent.CollideHandler(){
            @Override
            public void onEvent(CollideEvent event)
            {
                if (!world.isRemote && event.result != null )
                {
                    switch(event.result.typeOfHit)
                    {
                        case ENTITY:
                            if(event.result.entityHit instanceof EntityLivingBase && event.result.entityHit != player2)
                                skill_Instance.attack((EntityLivingBase) event.result.entityHit, damage, false);
                        case BLOCK:
                            ;
                    }
                }
            }
        });

    }

    public void setPlaceWhenCollide(boolean value)
    {
        placeWhenCollide = value;
    }

    @Override
    public void onUpdate()
    {
        try {
            super.onUpdate();
        }
        catch (NullPointerException e){
            AcademyMonster.log.info("Found a danger block manipulated, please report to author.");
            e.printStackTrace();
        }
        yaw += yawSpeed;
        pitch += pitchSpeed;

        if (this.world.isRemote) {
            this.actionType = this.dataManager.get(ACTION_TYPE);
            this.tx = this.dataManager.get(TX);
            this.ty = this.dataManager.get(TY);
            this.tz = this.dataManager.get(TZ);
        } else
        {
            this.dataManager.set(ACTION_TYPE, actionType);
            this.dataManager.set(TX, tx);
            this.dataManager.set(TY, ty);
            this.dataManager.set(TZ, tz);
        }
        switch (actionType)
        {
            case ActMoveTo:
            {
                double dist = this.getDistanceSq(tx, ty, tz);
                Vec3d delta = new Vec3d(tx - posX, ty - posY, tz - posZ).normalize();
                Vec3d mo = VecUtils.multiply(delta,.2 * ((dist<4)?dist/4:1.0));
                VecUtils.setMotion(this, mo);
            }
            case ActNothing:
                motionY -= 0.04;
        }

        posX += motionX;
        posY += motionY;
        posZ += motionZ;
    }

}
