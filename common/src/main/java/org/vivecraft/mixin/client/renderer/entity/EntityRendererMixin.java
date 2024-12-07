package org.vivecraft.mixin.client.renderer.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void vivecraft$storeUUID(Entity entity, EntityRenderState entityRenderState, float partialTick, CallbackInfo ci) {
        // set the UUID so we can identify what entity is rendered
        ((EntityRenderStateExtension)entityRenderState).vivecraft$setEntityUUID(entity.getUUID());
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getRopeHoldPosition(F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 vivecraft$getRopeHoldPosition(Entity instance, float f, Operation<Vec3> original) {
        if (!RenderPassType.isVanilla() && instance == Minecraft.getInstance().player) {
            return RenderHelper.getControllerRenderPos(0);
        } else {
            return original.call(instance, f);
        }
    }
}
