package dev.fixedvoiceport.mixin;

import dev.fixedvoiceport.PortConfig;
import dev.fixedvoiceport.VoicePortAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "de.maxhenkel.voicechat.voice.server.Server", remap = false)
public abstract class VoiceServerMixin implements VoicePortAccess {
    @Shadow @Final private boolean dedicated;
    @Shadow private int port;
    @Shadow public abstract void changePort(int port) throws Exception;
    @Unique private boolean fixedVoicePort$allowChange;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void fixedVoicePort$setPort(CallbackInfo ci) {
        if (!dedicated) {
            port = PortConfig.load();
        }
    }

    @Inject(method = "changePort(I)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void fixedVoicePort$keepPort(int requestedPort, CallbackInfo ci) {
        if (!dedicated && !fixedVoicePort$allowChange) {
            ci.cancel();
        }
    }

    @Override
    public void fixedVoicePort$selectPort(int selectedPort) throws Exception {
        PortConfig.validate(selectedPort);
        if (dedicated) {
            throw new IllegalStateException("The LAN port selector is only for integrated servers");
        }
        if (selectedPort != port) {
            fixedVoicePort$allowChange = true;
            try {
                changePort(selectedPort);
            } finally {
                fixedVoicePort$allowChange = false;
            }
        }
    }
}
