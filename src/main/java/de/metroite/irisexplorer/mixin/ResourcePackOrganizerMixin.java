package de.metroite.irisexplorer.mixin;

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
    private List<String> packs_open;
    private Boolean init = false;

    @Mutable
    @Shadow @Final private List<ResourcePackProfile> enabledPacks;

    @Shadow @Final private List<ResourcePackProfile> disabledPacks;

    // On ResourcePackScreen Close
    @Inject(method = "apply", at = @At(value = "HEAD"), cancellable = true)
    private void irisExplorerOnScreenClosed(CallbackInfo ci) {
        //resourcepack priority after any change
        List<String> packs_close = irisExplorerGetEnabledPacksAsString();

        //Priority Map
        IrisPackManager.RESOURCE_PACK_PRIORITY_MAP.clear();
        for(int i = 0; i < packs_close.size(); i++) {
            IrisPackManager.RESOURCE_PACK_PRIORITY_MAP.put(packs_close.get(i), packs_close.size() - i);
        }

        List<String> rppacks_open = irisExplorerFilterOutShaderpacks(packs_open);
        List<String> rppacks_close = irisExplorerFilterOutShaderpacks(packs_close);

        if (irisExplorerHasShaderpacks(packs_close)) {
            List<String> sppacks_open = irisExplorerFilterOutResourcepacks(packs_open);
            List<String> sppacks_close = irisExplorerFilterOutResourcepacks(packs_close);

            //save current pack layout
            IrisPackManager.setEnabledPacks(this.enabledPacks);

            //cancel reloading shaders if shaderpacks weren't moved
            if (sppacks_open != null) {
                if (!sppacks_open.equals(sppacks_close)) {
                    //IrisExplorerMod.LOGGER.info("Switching shaderpack");
                    IrisExplorerMod.reload();
                }

            } else if (sppacks_close != null) {
                //IrisExplorerMod.LOGGER.info("Switching from internal shader");
                IrisExplorerMod.reload();
            }

        //no selected shaderpacks -> fallback to internal shaders
        } else if (!IrisPackManager.getCurrentShaderpack().equals("(internal)")) {
            //reload shaderpacks with default
            IrisPackManager.setCurrentShaderpack("(internal)");
            try {
                Iris.reload();
            } catch (IOException e) {
                IrisExplorerMod.LOGGER.error("Error while reloading Shaders for Iris!", e);
            }
        }

        //cancel reloading resources if resourcepacks weren't moved
        if (rppacks_open != null) {
            if (rppacks_open.equals(rppacks_close)) {
                IrisExplorerMod.LOGGER.warn("Cancelling resource reload");

                ci.cancel();
            }
        }

        //prepare for next call
        init = false;
    }

    // Keep Shaderpacks in EnabledPacks
    @Inject(method = "getEnabledPacks", at = @At(value = "HEAD"))
    private void irisExplorerLoadEnabledPacks(CallbackInfoReturnable<Stream<ResourcePackOrganizer.Pack>> cir) {
        //load saved layout
        if (!init) {
            //Initial game start, mark previously set shaderpack as enabled
            if (IrisPackManager.getEnabledPacks() == null) {
                if (!IrisPackManager.getShaderpack().equals("(internal)")) {

                    List<ResourcePackProfile> init_pack = this.disabledPacks.stream().filter((pack) -> pack.getDisplayName().
                            getString().equals(IrisPackManager.PACKID + "/" + IrisPackManager.getPackSubID(IrisPackManager.
                            getShaderpack()))).collect(Collectors.toList());

                    if (init_pack.size() > 0) {
                        int index = this.disabledPacks.indexOf(init_pack.get(0));
                        this.enabledPacks.add(0, this.disabledPacks.remove(index));
                    }
                }
                //save initial pack layout (Reloads resources on every initial game start if not available)
                IrisPackManager.setEnabledPacks(this.enabledPacks);
            }

            //load savedPacks
            this.enabledPacks = IrisPackManager.getEnabledPacks();
            //remove all enabled packs from disabled packs list
            this.disabledPacks.removeAll(IrisPackManager.getEnabledPacks());
            /*
            //remove all deleted packs from disabled packs list
            if (IrisPackManager.removedPacks != null && IrisPackManager.removedPacks.size() > 0) {
                this.disabledPacks.removeAll(this.disabledPacks.stream().filter((pack) -> {
                    IrisExplorerMod.LOGGER.error(pack.getDisplayName().getString());
                    return IrisPackManager.removedPacks.stream().map((rempack) -> {
                        IrisExplorerMod.LOGGER.error(IrisPackManager.PACKID + "/" + IrisPackManager.getPackSubID(rempack));
                        return IrisPackManager.PACKID + "/" + IrisPackManager.getPackSubID(rempack);
                    }).collect(Collectors.toList()).contains(pack.getDisplayName().getString());
                }).collect(Collectors.toList()));
            }
             */
            //save initial pack layout
            packs_open = irisExplorerGetEnabledPacksAsString();

            init = true; //once
        }
    }

    private List<String> irisExplorerGetEnabledPacksAsString() {
        return this.enabledPacks.stream().map(ResourcePackProfile::getName).collect(Collectors.toList());
    }

    private List<String> irisExplorerFilterOutShaderpacks(List<String> packList) {
        if (packList == null) {
            return null;
        }
        List<String> shaderpacks = irisExplorerGetShaderpacks();
        return packList.stream().filter(pack -> !shaderpacks.contains(pack)).collect(Collectors.toList());
    }

    private List<String> irisExplorerFilterOutResourcepacks(List<String> packList) {
        if (packList == null) {
            return null;
        }
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
        return IrisPackManager.registeredPacks.stream().map(spack -> IrisPackManager.PACKID + "/" + IrisPackManager.getPackSubID(spack)).collect(Collectors.toList());
    }
}
