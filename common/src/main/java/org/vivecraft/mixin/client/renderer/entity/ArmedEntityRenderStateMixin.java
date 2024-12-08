package org.vivecraft.mixin.client.renderer.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.trackers.ClimbTracker;

@Mixin(ArmedEntityRenderState.class)
public class ArmedEntityRenderStateMixin {
    @WrapOperation(method = "extractArmedEntityRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemHeldByArm(Lnet/minecraft/world/entity/HumanoidArm;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack vivecraft$clawOverride(LivingEntity instance, HumanoidArm humanoidArm, Operation<ItemStack> original) {
        ItemStack thisStack = original.call(instance, humanoidArm);

        ClimbTracker tracker = ClientDataHolderVR.getInstance().climbTracker;
        if (ClientNetworking.serverAllowsClimbey && VRPlayersClient.getInstance().isTracked(instance.getUUID()) && !tracker.isClaws(thisStack)) {
            ItemStack otherStack = original.call(instance, humanoidArm.getOpposite());
            if (tracker.isClaws(otherStack) && !VRPlayersClient.getInstance().getRotationsForPlayer(instance.getUUID()).seated) {
                return otherStack;
            }
        }

        return thisStack;
    }
}
