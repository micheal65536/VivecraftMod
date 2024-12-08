package org.vivecraft.mixin.client_vr.renderer.item.properties.conditional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.IsUsingItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.VRState;

@Mixin(IsUsingItem.class)
public class IsUsingItemVRMixin {

    @Inject(method = "get", at = @At(value = "HEAD"), cancellable = true)
    private void vivecraft$noHornUseAnimFabric(ItemStack itemStack, ClientLevel clientLevel, LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext, CallbackInfoReturnable<Boolean> cir) {
        if (VRState.vrRunning && itemStack.is(Items.GOAT_HORN) && livingEntity == Minecraft.getInstance().player) {
            cir.setReturnValue(false);
        }
    }
}
