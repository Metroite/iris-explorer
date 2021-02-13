package de.metroite.irisexplorer;

import com.google.common.base.Throwables;
import de.metroite.irisexplorer.config.IrisExplorerConfig;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.coderbot.iris.Iris;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class IrisExplorerMod implements ClientModInitializer {
    public static final String MODID = "iris-explorer";
    public static IrisExplorerConfig CONFIG;
    public static final Object2IntMap<String> RESOURCE_PACK_PRIORITY_MAP = new Object2IntOpenHashMap<>();
    public static final Logger LOGGER = LogManager.getLogger("iris-explorer");
    public static String SHADER = "(internal)";

    @Override
    public void onInitializeClient() {
        //  Config
        AutoConfig.register(IrisExplorerConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(IrisExplorerConfig.class).getConfig();
        // /Config



        //  Keybind (R)
        ClientTickEvents.END_CLIENT_TICK.register((minecraftClient) -> {
            if (Iris.reloadKeybind.wasPressed()) {
                reloadIrisExplorer();
                try {
                    Iris.reload();
                    if (minecraftClient.player != null) {
                        minecraftClient.player.sendMessage(new TranslatableText("iris.shaders.reloaded"), false);
                    }
                } catch (Exception var2) {
                    Iris.logger.error("Error while reloading Shaders for Iris!", var2);
                    if (minecraftClient.player != null) {
                        minecraftClient.player.sendMessage((new TranslatableText("iris.shaders.reloaded.failure", new Object[]{Throwables.getRootCause(var2).getMessage()})).formatted(Formatting.RED), false);
                    }
                }
            }

        });
        // /Keybind



        //  Ressourceupdate (F3 + T)
        //setupClientReloadListeners();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(MODID, "client_update_shader");
            }

            @Override
            public void apply(ResourceManager manager) {
                reloadIrisExplorer();
                try {	//This doesn't reload properly for some reason
                    Iris.reload();
                } catch (IOException e) {
                    Iris.logger.error("Error while reloading Shaders for Iris! (F3 + T)", e);
                }
            }
        });
        // /Ressourceupdate
    }

    public static void reloadIrisExplorer() {
        if (CONFIG.toggle) {
            IrisPackManager.updateShaderpackList();
            SHADER = IrisPackManager.getShaderpack();
            IrisPackManager.setShaderpack(SHADER);
            LOGGER.info("Shaderpack list updated! Current shaderpack: " + SHADER);
        }
    }

    static {
        RESOURCE_PACK_PRIORITY_MAP.defaultReturnValue(-1);
    }
}