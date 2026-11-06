package dev.fixedvoiceport.mixin;

import de.maxhenkel.voicechat.Voicechat;
import dev.fixedvoiceport.PortConfig;
import dev.fixedvoiceport.VoicePortAccess;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.MultiplayerOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Layout adapter for the modern world settings screen. */
@Mixin(value = MultiplayerOptionsScreen.class, remap = false)
public abstract class ShareToLanScreenMixin extends Screen {
    @Shadow private EditBox portEdit;
    @Shadow private Button applyChanges;
    @Shadow private void updateApplyChangesActiveState() { throw new AssertionError(); }
    @Unique private EditBox fixedVoicePort$field;
    @Unique private int fixedVoicePort$initial;

    protected ShareToLanScreenMixin(Component title) { super(title); }

    // Nest in the existing port cell, so resizing and scrolling use vanilla layout.
    @ModifyArg(method = "init", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"), index = 0)
    private LayoutElement fixedVoicePort$insertField(LayoutElement element) {
        if (element != portEdit) return element;
        String value;
        if (fixedVoicePort$field == null) {
            fixedVoicePort$initial = PortConfig.load();
            value = String.valueOf(fixedVoicePort$initial);
        } else {
            value = fixedVoicePort$field.getValue();
        }
        Component label = Component.translatable("fixed_voice_port.voice_port");
        fixedVoicePort$field = new EditBox(font, label);
        fixedVoicePort$field.setWidth(portEdit.getWidth());
        fixedVoicePort$field.setMaxLength(5);
        fixedVoicePort$field.setValue(value);
        fixedVoicePort$field.setTooltip(Tooltip.create(Component.translatable("fixed_voice_port.tooltip")));
        fixedVoicePort$field.setResponder(text -> {
            fixedVoicePort$validate();
            updateApplyChangesActiveState();
        });
        LinearLayout cell = LinearLayout.vertical().spacing(4);
        cell.addChild(portEdit);
        cell.addChild(new StringWidget(label, font));
        cell.addChild(fixedVoicePort$field);
        return cell;
    }

    @Inject(method = "hasSettingsChanges", at = @At("RETURN"), cancellable = true)
    private void fixedVoicePort$includeVoiceChange(CallbackInfoReturnable<Boolean> cir) {
        if (fixedVoicePort$field != null && fixedVoicePort$validate()
                && Integer.parseInt(fixedVoicePort$field.getValue()) != fixedVoicePort$initial) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "updateApplyChangesActiveState", at = @At("TAIL"))
    private void fixedVoicePort$disableInvalid(CallbackInfo ci) {
        if (applyChanges != null && fixedVoicePort$field != null && !fixedVoicePort$validate()) {
            applyChanges.active = false;
        }
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;"), index = 1)
    private Button.OnPress fixedVoicePort$wrapApply(Button.OnPress original) {
        return button -> {
            if (fixedVoicePort$field != null && button.getMessage().getString().equals(
                    Component.translatable("menu.multiplayerOptions.applyChanges").getString())) {
                if (!fixedVoicePort$validate()) return;
                int selected = Integer.parseInt(fixedVoicePort$field.getValue());
                try {
                    Object server = Voicechat.SERVER.getServer();
                    if (!(server instanceof VoicePortAccess access)) {
                        fixedVoicePort$setError("fixed_voice_port.unavailable");
                        return;
                    }
                    access.fixedVoicePort$selectPort(selected);
                    PortConfig.save(selected);
                } catch (Exception e) {
                    System.err.println("[Fixed Simple VC Port] Cannot apply port " + selected + ": " + e);
                    fixedVoicePort$setError("fixed_voice_port.apply_error");
                    return;
                }
            }
            original.onPress(button);
        };
    }

    @Unique private boolean fixedVoicePort$validate() {
        try {
            PortConfig.validate(Integer.parseInt(fixedVoicePort$field.getValue()));
            fixedVoicePort$field.setTextColor(0xFFE0E0E0);
            fixedVoicePort$field.setTooltip(Tooltip.create(Component.translatable("fixed_voice_port.tooltip")));
            return true;
        } catch (IllegalArgumentException e) {
            fixedVoicePort$setError("fixed_voice_port.invalid");
            return false;
        }
    }

    @Unique private void fixedVoicePort$setError(String key) {
        fixedVoicePort$field.setTextColor(0xFFFF5555);
        fixedVoicePort$field.setTooltip(Tooltip.create(Component.translatable(key)));
    }
}
