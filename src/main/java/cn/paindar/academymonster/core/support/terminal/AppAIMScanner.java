package cn.paindar.academymonster.core.support.terminal;

import cn.academy.terminal.App;
import cn.academy.terminal.AppEnvironment;
import cn.academy.terminal.RegApp;
import cn.lambdalib2.registry.RegistryCallback;
import cn.paindar.academymonster.core.support.terminal.ui.AIMScannerUI;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Paindar on 2017/2/13.
 */
public class AppAIMScanner extends App {

    public static final cn.academy.item.ItemApp app_aim_scanner = new cn.academy.item.ItemApp("aim_scanner");

    @RegApp
    public static AppAIMScanner instance = new AppAIMScanner();

    private AppAIMScanner() {
        super("aim_scanner");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AppEnvironment createEnvironment() {
        return new AppEnvironment() {
            @Override
            @SideOnly(Side.CLIENT)
            public void onStart() {
                AIMScannerUI.keyHandler.onKeyUp();
            }
        };
    }

    @RegistryCallback
    private static void registerItems(RegistryEvent.Register<Item> event) {
        app_aim_scanner.setRegistryName("academymonster:aim_scanner");
        app_aim_scanner.setTranslationKey("ac_apps");
        app_aim_scanner.setCreativeTab(cn.academy.AcademyCraft.cct);
        event.getRegistry().register(app_aim_scanner);

        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            registerItemRenderers();
        }
    }

    @SideOnly(Side.CLIENT)
    private static void registerItemRenderers() {
        ModelLoader.setCustomModelResourceLocation(app_aim_scanner, 0, new ModelResourceLocation("academymonster:aim_scanner", "inventory"));
    }
}
