package org.vivecraft.mixin.client_vr.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TutorialToast.class)
public abstract class TutorialToastVRMixin implements Toast {

    @Shadow
    @Final
    private Component title;

    @Shadow
    @Final
    private Component message;

    @Unique
    private int vivecraft$offset;

    @Inject(at = @At("HEAD"), method = "render")
    private void vivecraft$extendToast(GuiGraphics guiGraphics, Font font, long l, CallbackInfo ci) {
        int width = Math.max(font.width(this.title), message != null ? font.width(this.message) : 0) + 34;
        vivecraft$offset = Math.min(this.width() - width, 0);
    }

    // change toast size
    // the texture gets stretched, but there seems to be no way to cut it in pieces, so that is probably the best option
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIII)V"), method = "render", index = 2)
    private int vivecraft$offsetToast(int x) {
        return x + vivecraft$offset;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIII)V"), method = "render", index = 4)
    private int vivecraft$changeToastWidth(int width) {
        return width - vivecraft$offset;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;render(Lnet/minecraft/client/gui/GuiGraphics;II)V"), method = "render", index = 1)
    private int vivecraft$offsetIcon(int x) {
        return x + vivecraft$offset;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"), method = "render", index = 2)
    private int vivecraft$offsetText(int x) {
        return x + vivecraft$offset;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"), method = "render", index = 0)
    private int vivecraft$offsetProgressStart(int x) {
        return x + vivecraft$offset;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 1), method = "render", index = 2)
    private int vivecraft$offsetProgressEnd(int x) {
        return x + vivecraft$offset - (int) ((float) x / TutorialToast.PROGRESS_BAR_WIDTH * vivecraft$offset);
    }
}
