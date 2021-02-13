package de.metroite.irisexplorer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class IrisPackManager {
    private static final Path shaderpacksDirectory = FabricLoader.getInstance().getGameDir().resolve("shaderpacks");
    private static final Path irisConfig = FabricLoader.getInstance().getConfigDir().resolve("iris.properties");
    public static final String PACKID = "iris";


    static String[] getShaderpackPaths() {
        String[] pathnames;
        File f = new File(shaderpacksDirectory + "");
        pathnames = f.list();
        return pathnames;
    }

    public static String getShaderpack() {
        String[] packs = getShaderpackPaths();
        int priority = -1;
        int currentPriority;
        String returnPack = "(internal)";
        for (String pack: packs) {
            currentPriority = IrisExplorerMod.RESOURCE_PACK_PRIORITY_MAP.getInt(PACKID + "/" + getPackSubID(pack));
            //if any higher priority shaderpack was found, switch to it
            if (currentPriority > priority) {
                returnPack = pack;
                priority = currentPriority;
            }
        }
        return returnPack;
    }

    public static void setShaderpack(String pack) {
        try {
            FileInputStream in = new FileInputStream(irisConfig.toString());
            Properties props = new Properties();
            props.load(in);
            in.close();

            FileOutputStream out = new FileOutputStream(irisConfig.toString());
            props.setProperty("shaderPack", pack);
            props.store(out, null);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  static void updateShaderpackList() {
        String[] packs = getShaderpackPaths();
        for (String pack:
             packs) {
            createResourcepack(pack);
        }
    }

    @SuppressWarnings({"deprecation"})        //I need exactly this functionality of registerBuiltinResourcePack
    static void createResourcepack(String pack) {
        FabricLoader.getInstance().getModContainer(IrisExplorerMod.MODID).ifPresent(modContainer -> {
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier(PACKID + ":" + getPackSubID(pack)), "resourcepacks/shaderpack", modContainer, false);
        });
    }

    static String getPackSubID(String pack) {
        return pack.toLowerCase().replaceAll("\\s+", "-");
    }

}
