package de.metroite.irisexplorer.mixin;

import de.metroite.irisexplorer.IrisExplorerMod;
import de.metroite.irisexplorer.IrisPackManager;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceReloadMonitor;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Mixin(ReloadableResourceManagerImpl.class)
public abstract class ReloadableResourceManagerImplMixin implements ReloadableResourceManager {
    //Creates Resourcepack Priority Map - code by NuclearFarts https://github.com/Nuclearfarts/connected-block-textures/blob/1b88d909a13125fbf602ed52d7d62a19c12cbc27/src/main/java/io/github/nuclearfarts/cbt/mixin/ReloadableResourceManagerImplMixin.java#L28
    @Inject(method = "beginMonitoredReload", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void irisResourcePackPriorityMap(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<ResourcePack> packs, CallbackInfoReturnable<ResourceReloadMonitor> cir) {
        IrisPackManager.RESOURCE_PACK_PRIORITY_MAP.clear();
        for(int i = 0; i < packs.size(); i++) {
            IrisPackManager.RESOURCE_PACK_PRIORITY_MAP.put(packs.get(i).getName(), i); //.substring(IrisPackManager.PACKID.length() + 1)
        }
        IrisExplorerMod.reload();
    }

    //Trying to cancel resource reload if its just shaderpacks being moved
    @Inject(method = "addPack", cancellable = true, at = @At(value = "HEAD"))
    private void irisRemoveShaderpacksFromResourceRelaoder(ResourcePack pack, CallbackInfo ci) {
        if (IrisPackManager.getShaderpackPaths().stream().map(spack -> IrisPackManager.PACKID + "/" + spack.toLowerCase().replaceAll("\\s+", "-")).collect(Collectors.toList()).contains(pack.getName())) { //Java is disgusting
            ci.cancel();
        }
    }
}