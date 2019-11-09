package cn.paindar.academymonster.core.support.terminal.ui;

import cn.lambdalib2.cgui.component.DrawTexture;
import cn.lambdalib2.cgui.event.FrameEvent;
import cn.lambdalib2.util.Colors;
import cn.lambdalib2.util.HudUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class DrawTextureEx extends DrawTexture
{
    private int shaderId = 0;
    public static int cound = 0;
    public static ResourceLocation res = new ResourceLocation("academy","textures/tutorial/ability_ui.png");
    public DrawTextureEx()
    {
        super("DrawTexture", res, Colors.white());
        cound++;
        System.out.println(cound);
        listen(FrameEvent.class, (w, e) ->
        {
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glDisable(GL_ALPHA_TEST);
            glDepthMask(writeDepth);
            glUseProgram(shaderId);
            if (depthTestMode == DepthTestMode.Equals) {
                GL11.glEnable(GL_DEPTH_TEST);
                GL11.glDepthFunc(GL_EQUAL);
            } else if (writeDepth) {
                GL11.glEnable(GL_DEPTH_TEST);
                GL11.glDepthFunc(GL_ALWAYS);
            } else {
                GL11.glDisable(GL_DEPTH_TEST);
            }

            Colors.bindToGL(color);

            if (zLevel != 0) {
                GL11.glPushMatrix();
                GL11.glTranslated(0, 0, zLevel);
            }

            if(texture != null && !texture.getPath().equals("<null>")) {
                Minecraft.getMinecraft().renderEngine.bindTexture(texture);
                if (doesUseUV) {
                    HudUtils.rect(0, 0, u, v, w.transform.width, w.transform.height, texWidth, texHeight);
                } else {
                    HudUtils.rect(0, 0, w.transform.width, w.transform.height);
                }
            } else {

                Minecraft.getMinecraft().renderEngine.bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
                HudUtils.colorRect(0, 0, w.transform.width, w.transform.height);
            }

            if (zLevel != 0) {
                GL11.glPopMatrix();
            }

            GL11.glDisable(GL_DEPTH_TEST);
            GL11.glDepthFunc(GL_LEQUAL);

            glUseProgram(0);
            glDepthMask(true);
        });
    }
}