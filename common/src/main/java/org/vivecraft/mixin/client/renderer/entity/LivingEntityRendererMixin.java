package org.vivecraft.mixin.client.renderer.entity;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER))
    private void vivecraft$vrPlayerHeightScale(CallbackInfo ci, @Local(argsOnly = true) LivingEntityRenderState state, @Local(argsOnly = true) PoseStack poseStack) {
        // need to do this here, because doing it in the VRPlayerRenderer doesn't work,
        // because forge/neoforge override the render method, and arch can't link that correctly
        if (VRPlayersClient.getInstance().isTracked(((EntityRenderStateExtension)state).vivecraft$getEntityUUID())) {
            VRPlayersClient.RotInfo rotInfo = VRPlayersClient.getInstance().getRotationsForPlayer(((EntityRenderStateExtension)state).vivecraft$getEntityUUID());
            poseStack.scale(rotInfo.heightScale, rotInfo.heightScale, rotInfo.heightScale);
        }
    }
}
