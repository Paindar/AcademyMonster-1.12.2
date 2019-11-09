package cn.paindar.academymonster.core.support.terminal.ui;

import cn.academy.ability.Skill;
import cn.academy.datapart.AbilityData;
import cn.academy.terminal.TerminalData;
import cn.academy.util.RegACKeyHandler;
import cn.lambdalib2.auxgui.AuxGui;
import cn.lambdalib2.auxgui.AuxGuiHandler;
import cn.lambdalib2.cgui.CGui;
import cn.lambdalib2.cgui.Widget;
import cn.lambdalib2.cgui.WidgetContainer;
import cn.lambdalib2.cgui.component.DrawTexture;
import cn.lambdalib2.cgui.component.TextBox;
import cn.lambdalib2.cgui.loader.CGUIDocument;
import cn.lambdalib2.input.KeyHandler;
import cn.lambdalib2.input.KeyManager;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.render.font.IFont;
import cn.lambdalib2.util.EntitySelectors;
import cn.lambdalib2.util.HudUtils;
import cn.lambdalib2.util.Raytrace;
import cn.lambdalib2.util.RenderUtils;
import cn.paindar.academymonster.core.support.terminal.AppAIMScanner;
import cn.paindar.academymonster.entity.datapart.MobSkillData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;
import org.lwjgl.util.glu.GLU;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paindar on 2017/2/13.
 */

@SideOnly(Side.CLIENT)
public class AIMScannerUI extends AuxGui
{
    private static AuxGui current = null;
    private static WidgetContainer loaded ;
    private Entity lastFocus=null;
    private Entity focus=null;
    private List<Widget> existedList=new ArrayList<>();
    private static Widget skillItem;

    @RegACKeyHandler(name = "open_aim_scanner", keyID = Keyboard.KEY_H)
    public static KeyHandler keyHandler = new KeyHandler()
    {
        @Override
        public void onKeyUp()
        {
            EntityPlayer player = getPlayer();
            TerminalData tData = TerminalData.get(player);
            if(tData.isTerminalInstalled())
            {
                if(tData.getInstalledApps().indexOf(AppAIMScanner.instance)!=-1)
                {
                    if (current == null || current.disposed)
                    {
                        current = new AIMScannerUI();
                        AuxGuiHandler.register(current);
                    } else if (current instanceof AIMScannerUI)
                    {
                        current.dispose();
                        current = null;
                    }
                }
                else
                    player.sendMessage(new TextComponentTranslation("am.aim_scanner_skill_list.notinstalled"));
            }

        }

    };


    @StateEventCallback
    public static void __init(FMLInitializationEvent evt)
    {

        loaded = CGUIDocument.read(new ResourceLocation("academymonster:gui/aim_scanner_ui.xml"));
            /*
        loaded=new WidgetContainer();
        Widget board=new Widget().size(640,340);
        loaded.addWidget("backBroad", board,true);
        //.addComponent(new Transform().setSize(640,340).setPos(0,0).setAlign(Transform.WidthAlign.LEFT, Transform.HeightAlign.TOP))
        //textures/gui/aim_scanner_back.png
        board.addComponent(new DrawTextureEx()
                .setColor(new Color(255,255,255,255)));
        board.addWidget("monsterName",new Widget(30,30,640,100)
                .addComponent(new TextBox(new IFont.FontOption(45, IFont.FontAlign.CENTER,new Color(255,255,255,168))).setContent(""))
                );
        board.addWidget("Skill Label",new Widget(30,60,640,100)
                .addComponent(new TextBox(
                new IFont.FontOption(
                        35, IFont.FontAlign.LEFT,new Color(255,255,255,168)))
                .setContent("Skill Name")));
        Widget skillList=new Widget(30,100,640,340);
        skillList.addWidget("skillItem",new Widget(30,30,640,50) .addComponent(
                new TextBox(
                        new IFont.FontOption(40, IFont.FontAlign.LEFT,
                                new Color(255,255,255,168))).setContent("A Test Skill")));

        board.addWidget("skill",skillList);

        CGUIDocument.write(loaded,new File("ui.xml"));
        */
    }

    private CGui gui = new CGui();
    private Widget root;

    public AIMScannerUI() {

        gui.addWidget(root = loaded.getWidget("backBroad").copy());
        requireTicking = true;
        foreground = false;
        {
            TextBox textBox = root.getWidget("Skill Label").getComponent(TextBox.class);
            textBox.setContent(I18n.translateToLocal("am.app.aim_scanner_skill_list.name"));
            Widget skillPart=root.getWidget("skill");
            skillItem = skillPart.getWidget("skillItem").copy();
            skillPart.removeWidget("skillItem");
        }
    }

    @Override
    public void onTick()
    {
        EntityPlayerSP player=Minecraft.getMinecraft().player;
        if(Minecraft.getMinecraft().world==null)
        {
            current.dispose();
            current=null;
            return ;
        }
        if(player == null)
            return ;
        RayTraceResult trace= Raytrace.traceLiving(player,20, EntitySelectors.living().and(EntitySelectors.exclude(player)));
        if(trace.typeOfHit == RayTraceResult.Type.ENTITY)
        {
            focus=trace.entityHit;
        }
        else
        {
            focus=null;
        }
        DrawTexture bg=DrawTexture.get(root);
        if(lastFocus!=focus)
        {
            while (!existedList.isEmpty())
            {
                existedList.get(0).dispose();
                existedList.remove(0);
            }
            if (focus == null)
            {
                root.getWidget("monsterName").getComponent(TextBox.class).setContent(I18n.translateToLocal("am.app.aim_scanner_no_focus.name"));
                bg.setColor(new Color(255,255,255,25));
            }
            else
            {
                bg.setColor(new Color(255,255,255,255));
                if( focus instanceof EntityPlayer)
                {
                    EntityPlayer tPlayer=(EntityPlayer) focus;
                    AbilityData data = AbilityData.get(tPlayer);
                    if(!data.hasCategory())
                    {
                        root.getWidget("monsterName").getComponent(TextBox.class).setContent(tPlayer.getDisplayName()+ " Level 0" );
                    }
                    else
                    {
                        root.getWidget("monsterName").getComponent(TextBox.class).setContent(tPlayer.getDisplayName()+" "+data.getCategory().getDisplayName() +" Level "+data.getLevel());
                        Widget list = root.getWidget("skill");
                        int num = 1;
                        for(Skill skill:data.getLearnedSkillList())
                        {

                            Widget widget = skillItem.copy().pos(30, 30 * num);
                            num++;
                            widget.getComponent(TextBox.class).setContent(I18n.translateToLocal(skill.getDisplayName()));
                            existedList.add(widget);
                            list.addWidget(widget);
                        }
                    }
                }
                else if(focus instanceof EntityMob)
                {
                    root.getWidget("monsterName").getComponent(TextBox.class).setContent(focus.getName());
                    String skill = MobSkillData.get((EntityMob) focus).getSkillData();
                    if (skill != null)
                    {
                        String[] skillName = skill.split("-");
                        Widget list = root.getWidget("skill");
                        int num = 1;
                        for (String item : skillName)
                        {
                            String[] data = item.split("~");

                            Widget widget = skillItem.copy().pos(30, 30 * num);
                            num++;
                            if (data.length == 2)
                            {
                                widget.getComponent(TextBox.class).setContent(I18n.translateToLocal(data[0]) );//+ String.format("(%.4f)", Float.parseFloat(data[1]))
                            } else if (data.length == 1)
                                widget.getComponent(TextBox.class).setContent(I18n.translateToLocal(data[0]));// + ("(0.00)")
                            else
                                continue;
                            existedList.add(widget);
                            list.addWidget(widget);
                        }
                    }
                }
            }
        }

    }

    @Override
    public void draw(ScaledResolution sr)
    {
        //gui.resize(sr.getScaledWidth(), sr.getScaledHeight());
        //GL11.glTranslated(-3, .5, -4);
        //gui.draw();

        /*
            I don't know why, but it works.
            下述三行代码解决一个错误的贴图载入问题。
        * */
        RenderUtils.loadTexture(new ResourceLocation("academymonster", "textures/items/railgun_core.png"));

        GL11.glColor4d(1, 1, 1, 0);
        HudUtils.rect( 1, 1);

        float aspect = (float) (sr.getScaledWidth_double() / sr.getScaledHeight());

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        {
            GLU.gluPerspective(50,
                    aspect,
                    1f, 100);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            {

                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glColor4d(1, 1, 1, 1);
                double scale = 1.0 / 310;

                GL11.glTranslated(-3, .5, -4);

                //GL11.glTranslated(1, -1.8, 0);

                GL11.glScaled(scale, -scale, scale);
                gui.draw();
                {
                    lastFocus = focus;
                }
            }
            GL11.glPopMatrix();
        }
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();

        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glCullFace(GL11.GL_BACK);
    }
}
