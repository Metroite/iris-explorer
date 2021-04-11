package de.metroite.irisexplorer;

import net.coderbot.iris.Iris;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class IrisExplorerMod {
    public static final String MODID = "iris-explorer";
    public static final Logger LOGGER = LogManager.getLogger("iris-explorer");

    /* This is done in ResourcePackOrganizerMixin instead
    @Override
    public void onInitializeClient() {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof PackScreen) {
                IrisPackManager.updateShaderpackList(); //Update Shaderpacks List in Resourcepack Screen
            }
        });
    }
     */

    public static void reload() {
        IrisPackManager.setCurrentShaderpack(IrisPackManager.getShaderpack());

        // Reload Iris' Engine
        try {
            Iris.reload();
        } catch (IOException e) {
            LOGGER.error("Error while reloading Shaders for Iris!", e);
        }
    }

}