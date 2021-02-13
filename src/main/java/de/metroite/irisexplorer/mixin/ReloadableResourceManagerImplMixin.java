package de.metroite.irisexplorer.mixin;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import de.metroite.irisexplorer.IrisExplorerMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceReloadMonitor;
import net.minecraft.util.Unit;

//code by NuclearFarts https://github.com/Nuclearfarts/connected-block-textures/blob/1b88d909a13125fbf602ed52d7d62a19c12cbc27/src/main/java/io/github/nuclearfarts/cbt/mixin/ReloadableResourceManagerImplMixin.java#L28

@Mixin(ReloadableResourceManagerImpl.class)
public abstract class ReloadableResourceManagerImplMixin implements ReloadableResourceManager {
    @Inject(method = "beginMonitoredReload", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void irisResourcePackPriorityMap(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<ResourcePack> packs, CallbackInfoReturnable<ResourceReloadMonitor> cir) {
        IrisExplorerMod.RESOURCE_PACK_PRIORITY_MAP.clear();
        for(int i = 0; i < packs.size(); i++) {
            IrisExplorerMod.RESOURCE_PACK_PRIORITY_MAP.put(packs.get(i).getName(), i); //.substring(IrisPackManager.PACKID.length() + 1)
        }
    }
}