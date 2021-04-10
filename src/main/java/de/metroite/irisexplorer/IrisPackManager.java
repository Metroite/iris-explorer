package de.metroite.irisexplorer;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class IrisPackManager {
    private static final Path shaderpacksDirectory = FabricLoader.getInstance().getGameDir().resolve("shaderpacks");
    private static final Path irisConfig = FabricLoader.getInstance().getConfigDir().resolve("iris.properties");
    public static final String PACKID = "iris";
    public static final Object2IntMap<String> RESOURCE_PACK_PRIORITY_MAP = new Object2IntOpenHashMap<>();
    //Used in Mixin to retain resourcepack/shaderpack layout
    private static List<ResourcePackProfile> enabledPacks = null;

    public static List<ResourcePackProfile> getEnabledPacks() {
        return enabledPacks;
    }

    public static void setEnabledPacks(List<ResourcePackProfile> enabledPacks) {
        IrisPackManager.enabledPacks = enabledPacks;
    }



    public static List<String> getShaderpackPaths() {
        List<String> pathnames;
        File f = new File(shaderpacksDirectory.toString());
        pathnames = Arrays.asList(Objects.requireNonNull(f.list()));
        return pathnames;
    }

    public static String getShaderpack() {
        List<String> packs = getShaderpackPaths();
        int priority = -1;
        int currentPriority;
        String returnPack = getCurrentShaderpack();
        for (String pack: packs) {
            currentPriority = RESOURCE_PACK_PRIORITY_MAP.getInt(PACKID + "/" + getPackSubID(pack));
            //if any higher priority shaderpack was found, switch to it
            if (currentPriority > priority) {
                returnPack = pack;
                priority = currentPriority;
            }
        }
        return returnPack;
    }

    public static String getCurrentShaderpack() {
        String prop = "(internal)";
        try {
            FileInputStream in = new FileInputStream(irisConfig.toString());
            Properties props = new Properties();
            props.load(in);
            in.close();

            prop = props.getProperty("shaderPack");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    public static void setCurrentShaderpack(String pack) {
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
        List<String> packs = getShaderpackPaths();
        for (String pack: packs) {
            createResourcepack(pack);
        }
    }

    @SuppressWarnings({"deprecation"})        //I need exactly this functionality of registerBuiltinResourcePack
    private static void createResourcepack(String pack) {
        FabricLoader.getInstance().getModContainer(IrisExplorerMod.MODID).ifPresent(modContainer -> {
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier(PACKID + ":" + getPackSubID(pack)), "resourcepacks/shaderpack", modContainer, false);
        });
    }

    private static String getPackSubID(String pack) {
        return pack.toLowerCase().replaceAll("\\s+", "-");
    }

    static {
        RESOURCE_PACK_PRIORITY_MAP.defaultReturnValue(-1);
    }
}
