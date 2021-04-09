package de.metroite.irisexplorer;

import net.coderbot.iris.Iris;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class IrisExplorerMod {
    public static final String MODID = "iris-explorer";
    public static final Logger LOGGER = LogManager.getLogger("iris-explorer");

    public static void reload() {
        IrisPackManager.setShaderpack(IrisPackManager.getShaderpack());

        // Reload Iris' Engine
        try {
            Iris.reload();
        } catch (IOException e) {
            LOGGER.error("Error while reloading Shaders for Iris!", e);
        }
    }
}