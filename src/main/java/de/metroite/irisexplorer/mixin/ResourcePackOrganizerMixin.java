package de.metroite.irisexplorer.mixin;

import com.google.common.collect.ImmutableList;
import de.metroite.irisexplorer.IrisExplorerMod;
import de.metroite.irisexplorer.IrisPackManager;
import net.coderbot.iris.Iris;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.resource.ResourcePackProfile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ResourcePackOrganizer.class)
public abstract class ResourcePackOrganizerMixin {
    private List<String> rppacks_open;
    private List<String> sppacks_open;
    private Boolean screen = false;
    private Boolean init = false;

    @Mutable
    @Shadow @Final private List<ResourcePackProfile> enabledPacks;

    @Shadow @Final private List<ResourcePackProfile> disabledPacks;

    // On ResourcePackScreen Close
    @Inject(method = "apply", at = @At(value = "HEAD"), cancellable = true)
    private void irisExplorerOnScreenClosed(CallbackInfo ci) {
        //resourcepack priority after any change
        List<String> packs = irisExplorerGetEnabledPacksAsString();
        boolean shaderpacksIncluded = irisExplorerHasShaderpacks(packs);

        //Priority Map
        IrisPackManager.RESOURCE_PACK_PRIORITY_MAP.clear();
        for(int i = 0; i < packs.size(); i++) {
            IrisPackManager.RESOURCE_PACK_PRIORITY_MAP.put(packs.get(i), packs.size() - i);
        }

        if (shaderpacksIncluded) {
            List<String> sppacks_close = irisExplorerFilterOutResourcepacks(packs);

            //save current layout
            IrisPackManager.setEnabledPacks(this.enabledPacks);

            //cancel reloading shaders nothing was moved
            if (sppacks_open != null) {
                if (!sppacks_open.equals(sppacks_close)) {
                    //IrisExplorerMod.LOGGER.info("Switching shaderpack");
                    IrisExplorerMod.reload();
                }
            } else if (sppacks_close != null) {
                //IrisExplorerMod.LOGGER.info("Switching from internal shader");
                IrisExplorerMod.reload();
            }

            List<String> rppacks_close = irisExplorerFilterOutShaderpacks(packs);
            //cancel reloading resources if only shaderpacks were moved
            if (rppacks_open.equals(rppacks_close)) {
                IrisExplorerMod.LOGGER.warn("Canceling resource reload");

                ci.cancel();
            }
        } else if (!IrisPackManager.getCurrentShaderpack().equals("(internal)")) {
            //reload shaderpacks with default
            IrisPackManager.setCurrentShaderpack("(internal)");
            try {
                Iris.reload();
            } catch (IOException e) {
                IrisExplorerMod.LOGGER.error("Error while reloading Shaders for Iris!", e);
            }
        }
        //prepare for next call
        screen = false;
        init = false;
    }

    // On ResourcePackScreen Open
    @Inject(method = "refresh", at = @At(value = "HEAD"))
    private void irisExplorerOnScreenOpen(CallbackInfo ci) {
        //we don't want it to refresh everytime
        if (!screen) {
            //resourcepack priority before any change
            List<String> packs = irisExplorerGetEnabledPacksAsString();
            rppacks_open = irisExplorerFilterOutShaderpacks(packs);

            IrisPackManager.updateShaderpackList();
            screen = true; //once
        }
    }

    // Keep Shaderpacks in EnabledPacks
    @Inject(method = "getEnabledPacks", at = @At(value = "HEAD"))
    private void irisExplorerLoadEnabledPacks(CallbackInfoReturnable<Stream<ResourcePackOrganizer.Pack>> cir) {
        //load saved layout
        if (!init) {
            List<ResourcePackProfile> savedPacks = IrisPackManager.getEnabledPacks();
            if (savedPacks != null) {
                this.enabledPacks = savedPacks;
                //remove all enabled packs from disable packs list
                this.disabledPacks.removeAll(savedPacks);

                //save current shaderpack layout (for skip shader reload)
                sppacks_open = irisExplorerFilterOutResourcepacks(irisExplorerGetEnabledPacksAsString());
            }
            init = true; //once
        }
    }

    private List<String> irisExplorerGetEnabledPacksAsString() {
        return this.enabledPacks.stream().map(ResourcePackProfile::getName).collect(ImmutableList.toImmutableList());
    }

    private List<String> irisExplorerFilterOutShaderpacks(List<String> packList) {
        List<String> shaderpacks = irisExplorerGetShaderpacks();
        return packList.stream().filter(pack -> !shaderpacks.contains(pack)).collect(Collectors.toList());
    }

    private List<String> irisExplorerFilterOutResourcepacks(List<String> packList) {
        List<String> shaderpacks = irisExplorerGetShaderpacks();
        return packList.stream().filter(pack -> shaderpacks.contains(pack)).collect(Collectors.toList());
    }

    private Boolean irisExplorerHasShaderpacks(List<String> packList) {
        List<String> shaderpacks = irisExplorerGetShaderpacks();

        boolean found = false;
        for (String pack:
             shaderpacks) {
            if (packList.contains(pack)) {
                found = true;
                break;
            }
        }

        return found;
    }

    private List<String> irisExplorerGetShaderpacks() {
        return IrisPackManager.getShaderpackPaths().stream().map(spack -> IrisPackManager.PACKID + "/" + spack.toLowerCase().replaceAll("\\s+", "-")).collect(Collectors.toList());
    }
}
