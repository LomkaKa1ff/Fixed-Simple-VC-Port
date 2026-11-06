package dev.fixedvoiceport.mixin;

import de.maxhenkel.voicechat.Voicechat;
import dev.fixedvoiceport.PortConfig;
import dev.fixedvoiceport.VoicePortAccess;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Minecraft 1.21.x LAN screen adapter (Mojang runtime names). */
@Mixin(value = ShareToLanScreen.class, remap = false)
public abstract class ShareToLanScreenMixin extends Screen {
    @Shadow private EditBox portEdit;
    @Unique private EditBox fixedVoicePort$field;
    @Unique private Component fixedVoicePort$error;

    protected ShareToLanScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void fixedVoicePort$addField(CallbackInfo ci) {
        // Keep text when resizing the window or changing GUI scale.
        String value = fixedVoicePort$field == null
                ? String.valueOf(PortConfig.load()) : fixedVoicePort$field.getValue();
        int y = Math.min(portEdit.getY() + 44, height - 50);
        fixedVoicePort$field = new EditBox(font, portEdit.getX(), y,
                portEdit.getWidth(), portEdit.getHeight(),
                Component.translatable("fixed_voice_port.voice_port"));
        fixedVoicePort$field.setMaxLength(5);
        fixedVoicePort$field.setResponder(text -> fixedVoicePort$validate());
        fixedVoicePort$field.setValue(value);
        fixedVoicePort$field.setTooltip(Tooltip.create(
                Component.translatable("fixed_voice_port.tooltip")));
        addRenderableWidget(fixedVoicePort$field);
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;"),
            index = 1)
    private Button.OnPress fixedVoicePort$wrapStart(Button.OnPress original) {
        return button -> {
            if (button.getMessage().getString().equals(Component.translatable("lanServer.start").getString())) {
                if (fixedVoicePort$field == null || !fixedVoicePort$validate()) {
                    return;
                }
                int selected = Integer.parseInt(fixedVoicePort$field.getValue());
                try {
                    Object server = Voicechat.SERVER.getServer();
                    if (!(server instanceof VoicePortAccess access)) {
                        fixedVoicePort$setError(Component.translatable("fixed_voice_port.unavailable"));
                        return;
                    }
                    access.fixedVoicePort$selectPort(selected);
                    PortConfig.save(selected);
                } catch (Exception e) {
                    System.err.println("[Fixed Voice Port] Cannot apply port " + selected + ": " + e);
                    fixedVoicePort$setError(Component.translatable("fixed_voice_port.apply_error"));
                    return;
                }
            }
            original.onPress(button);
        };
    }

    @Unique
    private boolean fixedVoicePort$validate() {
        if (fixedVoicePort$field == null) return false;
        try {
            PortConfig.validate(Integer.parseInt(fixedVoicePort$field.getValue()));
            fixedVoicePort$error = null;
            fixedVoicePort$field.setTextColor(0xFFE0E0E0);
            fixedVoicePort$field.setTooltip(Tooltip.create(
                    Component.translatable("fixed_voice_port.tooltip")));
            return true;
        } catch (IllegalArgumentException e) {
            fixedVoicePort$setError(Component.translatable("fixed_voice_port.invalid"));
            return false;
        }
    }

    @Unique
    private void fixedVoicePort$setError(Component message) {
        fixedVoicePort$error = message;
        fixedVoicePort$field.setTextColor(0xFFFF5555);
        fixedVoicePort$field.setTooltip(Tooltip.create(message));
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void fixedVoicePort$drawLabel(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                         float partialTick, CallbackInfo ci) {
        if (fixedVoicePort$field == null) return;
        int labelY = Math.max(portEdit.getY() + portEdit.getHeight(), fixedVoicePort$field.getY() - 14);
        graphics.centeredText(font, Component.translatable("fixed_voice_port.voice_port"),
                width / 2, labelY, 0xFFFFFFFF);
        if (fixedVoicePort$error != null && fixedVoicePort$field.getY() + 34 < height - 28) {
            graphics.centeredText(font, fixedVoicePort$error, width / 2,
                    fixedVoicePort$field.getY() + 25, 0xFFFF5555);
        }
    }
}
