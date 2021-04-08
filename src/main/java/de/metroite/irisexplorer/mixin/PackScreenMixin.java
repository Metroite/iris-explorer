package de.metroite.irisexplorer.mixin;

import de.metroite.irisexplorer.IrisPackManager;
import net.minecraft.client.gui.screen.pack.PackScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackScreen.class)
public class PackScreenMixin {
    @Inject(method = "init", at = @At(value = "HEAD"))
    private void irisOnPackScreenOpen(CallbackInfo ci) {
        IrisPackManager.updateShaderpackList();
    }
    /* Is instead handled in ReloadableResourceManagerImplMixin
    @Inject(method = "onClose", at = @At(value = "HEAD"))
    private void irisOnPackScreenClose(CallbackInfo ci) {
        IrisExplorerMod.reload();
    }
     */
}
