package org.vivecraft.mixin.client.renderer.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.trackers.ClimbTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.render.RenderPass;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
    @ModifyExpressionValue(method = "appendItemLayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object vivecraft$itemOverride(Object original, @Local(argsOnly = true) ItemStack itemStack) {
        if (VRState.vrRunning && ClientDataHolderVR.getInstance().currentPass != RenderPass.GUI && itemStack.is(Items.SPYGLASS)) {
            return TelescopeTracker.scopeModel;
        }
        if (ClientDataHolderVR.getInstance().climbTracker.isClaws(itemStack)) {
            return ClimbTracker.clawsModel;
        }
        return original;
    }
}
