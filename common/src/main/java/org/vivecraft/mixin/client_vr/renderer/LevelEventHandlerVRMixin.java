package org.vivecraft.mixin.client_vr.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

@Mixin(LevelEventHandler.class)
public class LevelEventHandlerVRMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(at = @At("HEAD"), method = "levelEvent")
    public void vivecraft$shakeOnSound(int i, BlockPos blockPos, int j, CallbackInfo ci) {
        boolean playerNearAndVR = VRState.vrRunning && this.minecraft.player != null && this.minecraft.player.isAlive() && this.minecraft.player.blockPosition().distSqr(blockPos) < 25.0D;
        if (playerNearAndVR) {
            switch (i) {
                /* pre 1.19.4, they are now separate
                case 1011,      // IRON_DOOR_CLOSE
                        1012,   // WOODEN_DOOR_CLOSE
                        1013,   // WOODEN_TRAPDOOR_CLOSE
                        1014,   // FENCE_GATE_CLOSE
                        1036    // IRON_TRAPDOOR_CLOSE
                        -> ClientDataHolderVR.getInstance().vr.triggerHapticPulse(0, 250);
                 */
                case 1019,      // ZOMBIE_ATTACK_WOODEN_DOOR
                     1020,   // ZOMBIE_ATTACK_IRON_DOOR
                     1021    // ZOMBIE_BREAK_WOODEN_DOOR
                    -> {
                    ClientDataHolderVR.getInstance().vr.triggerHapticPulse(0, 750);
                    ClientDataHolderVR.getInstance().vr.triggerHapticPulse(1, 750);
                }
                case 1030 ->    // ANVIL_USE
                    ClientDataHolderVR.getInstance().vr.triggerHapticPulse(0, 500);
                case 1031 -> {  // ANVIL_LAND
                    ClientDataHolderVR.getInstance().vr.triggerHapticPulse(0, 1250);
                    ClientDataHolderVR.getInstance().vr.triggerHapticPulse(1, 1250);
                }
            }
        }
    }
}
