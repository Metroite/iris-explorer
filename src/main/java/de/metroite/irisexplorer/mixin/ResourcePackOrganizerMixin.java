package de.metroite.irisexplorer.mixin;

import com.google.common.collect.ImmutableList;
import de.metroite.irisexplorer.IrisExplorerMod;
import de.metroite.irisexplorer.IrisPackManager;
import net.coderbot.iris.Iris;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.resource.ResourcePackProfile;
import org.spongepowered.asm.mixin.*;
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
    private List<String> packs_open;
    private Boolean screen = false;

    @Mutable
    @Shadow @Final private List<ResourcePackProfile> enabledPacks;

    @Shadow @Final private List<ResourcePackProfile> disabledPacks;

    // On ResourcePackScreen Close
    @Inject(method = "apply", at = @At(value = "HEAD"), cancellable = true)
    private void irisExplorerOnScreenClosed(CallbackInfo ci) {
        //resourcepack priority after any change
        List<String> packs = irisExplorerGetEnabledPacksAsString();
        boolean shaderpacksIncluded = irisExplorerHasShaderpacks(packs);
        List<String> packs_close;

        //Priority Map
        IrisPackManager.RESOURCE_PACK_PRIORITY_MAP.clear();
        for(int i = 0; i < packs.size(); i++) {
            IrisPackManager.RESOURCE_PACK_PRIORITY_MAP.put(packs.get(i), packs.size() - i);
        }

        if (shaderpacksIncluded) {
            packs_close = irisExplorerFilterOutShaderpacks(packs);

            //reload shaderpacks
            IrisExplorerMod.reload();

            //cancel reloading resourcepacks if only shaderpacks were moved
            if (packs_open.equals(packs_close)) {
                IrisExplorerMod.LOGGER.warn("Canceling resource reload");
                //save current layout
                IrisPackManager.setEnabledPacks(this.enabledPacks);

                ci.cancel();
            }
        } else {
            //reload shaderpacks with default
            IrisPackManager.setShaderpack("(internal)");
            try {
                Iris.reload();
            } catch (IOException e) {
                IrisExplorerMod.LOGGER.error("Error while reloading Shaders for Iris!", e);
            }
        }
        //prepare for next call
        screen = false;
    }

    // On ResourcePackScreen Open
    @Inject(method = "refresh", at = @At(value = "HEAD"))
    private void irisExplorerOnScreenOpen(CallbackInfo ci) {
        //we don't want it to refresh everytime
        if (!screen) {
            //resourcepack priority before any change
            packs_open = irisExplorerFilterOutShaderpacks(irisExplorerGetEnabledPacksAsString());

            IrisPackManager.updateShaderpackList();
            screen = true;
        }
    }

    // Keep Shaderpacks in EnabledPacks
    @Inject(method = "getEnabledPacks", at = @At(value = "HEAD"))
    private void irisExplorerLoadEnabledPacks(CallbackInfoReturnable<Stream<ResourcePackOrganizer.Pack>> cir) {
        //load saved layout
        List<ResourcePackProfile> savedPacks = IrisPackManager.getEnabledPacks();
        if (savedPacks != null) {
            this.enabledPacks = savedPacks;
            //remove all enabled packs from disable packs list
            this.disabledPacks.removeAll(savedPacks);
        }
    }

    private List<String> irisExplorerGetEnabledPacksAsString() {
        return this.enabledPacks.stream().map(ResourcePackProfile::getName).collect(ImmutableList.toImmutableList());
    }

    private List<String> irisExplorerFilterOutShaderpacks(List<String> packList) {
        List<String> shaderpacks = IrisPackManager.getShaderpackPaths().stream().map(spack -> IrisPackManager.PACKID + "/" + spack.toLowerCase().replaceAll("\\s+", "-")).collect(Collectors.toList());
        return packList.stream().filter(pack -> !shaderpacks.contains(pack)).collect(Collectors.toList());
    }

    private Boolean irisExplorerHasShaderpacks(List<String> packList) {
        List<String> shaderpacks = IrisPackManager.getShaderpackPaths().stream().map(spack -> IrisPackManager.PACKID + "/" + spack.toLowerCase().replaceAll("\\s+", "-")).collect(Collectors.toList());

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
}
