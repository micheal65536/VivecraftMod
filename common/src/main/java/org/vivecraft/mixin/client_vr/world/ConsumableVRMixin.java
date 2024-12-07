package org.vivecraft.mixin.client_vr.world;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

@Mixin(Consumable.class)
public class ConsumableVRMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canEat(Z)Z"), method = "canConsume", cancellable = true)
    private void vivecraft$alwaysAllowEasterEggEating(LivingEntity livingEntity, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (itemStack.getHoverName().getString().equals("EAT ME")) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "onConsume", at = @At("HEAD"))
    private void vivecraft$wonderFoodEasterEgg(Level level, LivingEntity livingEntity, ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        if (VRState.vrInitialized && livingEntity == Minecraft.getInstance().player) {
            if (itemStack.is(Items.POTION) && itemStack.getHoverName().getString().equals("DRINK ME")) {
                ClientDataHolderVR.getInstance().vrPlayer.wfMode = -0.05;
                ClientDataHolderVR.getInstance().vrPlayer.wfCount = 400;
            } else if (itemStack.get(DataComponents.FOOD) != null && itemStack.getHoverName().getString().equals("EAT ME")) {
                ClientDataHolderVR.getInstance().vrPlayer.wfMode = 0.5D;
                ClientDataHolderVR.getInstance().vrPlayer.wfCount = 400;
            }
        }
    }
}
