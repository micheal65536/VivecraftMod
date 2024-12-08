package org.vivecraft.mixin.client_vr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
import org.vivecraft.common.utils.math.Vector2;

import static org.vivecraft.client_vr.provider.openvr_lwjgl.control.VivecraftMovementInput.getMovementAxisValue;

@Mixin(KeyboardInput.class)
public class KeyboardInputVRMixin extends ClientInput {

    @Final
    @Shadow
    private Options options;
    @Unique
    private boolean vivecraft$autoSprintActive = false;
    @Unique
    private boolean vivecraft$movementSetByAnalog = false;

    @Unique
    private float vivecraft$axisToDigitalMovement(float value) {
        if (value > 0.5F) {
            return 1.0F;
        } else {
            return value < -0.5F ? -1.0F : 0.0F;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void vivecraft$tick(CallbackInfo ci) {
        if (!VRState.vrRunning) {
            return;
        }

        ci.cancel();

        this.leftImpulse = 0.0F;
        this.forwardImpulse = 0.0F;
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        boolean climbing = dataholder.climbTracker.isClimbeyClimb() && !minecraft.player.isInWater() && dataholder.climbTracker.isGrabbingLadder();

        boolean up, down, left, right, jumping, shiftKeyDown;
        boolean sprint = this.options.keySprint.isDown();


        if (climbing || !this.options.keyUp.isDown() && !VivecraftVRMod.INSTANCE.keyTeleportFallback.isDown()) {
            up = false;
        } else {
            ++this.forwardImpulse;
            up = true;
        }

        if (!climbing && this.options.keyDown.isDown()) {
            --this.forwardImpulse;
            down = true;
        } else {
            down = false;
        }

        if (!climbing && this.options.keyLeft.isDown()) {
            ++this.leftImpulse;
            left = true;
        } else {
            left = false;
        }

        if (!climbing && this.options.keyRight.isDown()) {
            --this.leftImpulse;
            right = true;
        } else {
            right = false;
        }

        boolean flag1 = false;
        float f = 0.0F;

        if (!climbing && !dataholder.vrSettings.seated && minecraft.screen == null && !KeyboardHandler.Showing) {
            VRInputAction vrinputaction = dataholder.vr.getInputAction(VivecraftVRMod.INSTANCE.keyFreeMoveStrafe);
            VRInputAction vrinputaction1 = dataholder.vr.getInputAction(VivecraftVRMod.INSTANCE.keyFreeMoveRotate);
            Vector2 vector2 = vrinputaction.getAxis2DUseTracked();
            Vector2 vector21 = vrinputaction1.getAxis2DUseTracked();

            if (vector2.getX() == 0.0F && vector2.getY() == 0.0F) {
                if (vector21.getY() != 0.0F) {
                    flag1 = true;
                    f = vector21.getY();

                    if (dataholder.vrSettings.analogMovement) {
                        this.forwardImpulse = vector21.getY();
                        this.leftImpulse = 0.0F;
                        this.leftImpulse -= getMovementAxisValue(this.options.keyRight);
                        this.leftImpulse += getMovementAxisValue(this.options.keyLeft);
                    } else {
                        this.forwardImpulse = this.vivecraft$axisToDigitalMovement(vector21.getY());
                    }
                } else if (dataholder.vrSettings.analogMovement) {
                    flag1 = true;
                    this.forwardImpulse = 0.0F;
                    this.leftImpulse = 0.0F;
                    float f1 = getMovementAxisValue(this.options.keyUp);

                    if (f1 == 0.0F) {
                        f1 = getMovementAxisValue(VivecraftVRMod.INSTANCE.keyTeleportFallback);
                    }

                    f = f1;
                    this.forwardImpulse += f1;
                    this.forwardImpulse -= getMovementAxisValue(this.options.keyDown);
                    this.leftImpulse -= getMovementAxisValue(this.options.keyRight);
                    this.leftImpulse += getMovementAxisValue(this.options.keyLeft);
                    float f2 = 0.05F;
                    this.forwardImpulse = Utils.applyDeadzone(this.forwardImpulse, f2);
                    this.leftImpulse = Utils.applyDeadzone(this.leftImpulse, f2);
                }
            } else {
                flag1 = true;
                f = vector2.getY();

                if (dataholder.vrSettings.analogMovement) {
                    this.forwardImpulse = vector2.getY();
                    this.leftImpulse = -vector2.getX();
                } else {
                    this.forwardImpulse = this.vivecraft$axisToDigitalMovement(vector2.getY());
                    this.leftImpulse = this.vivecraft$axisToDigitalMovement(-vector2.getX());
                }
            }

            if (flag1) {
                this.vivecraft$movementSetByAnalog = true;
                up = this.forwardImpulse > 0.0F;
                down = this.forwardImpulse < 0.0F;
                left = this.leftImpulse > 0.0F;
                right = this.leftImpulse < 0.0F;
                VRInputAction.setKeyBindState(this.options.keyUp, up);
                VRInputAction.setKeyBindState(this.options.keyDown, down);
                VRInputAction.setKeyBindState(this.options.keyLeft, left);
                VRInputAction.setKeyBindState(this.options.keyRight, right);

                if (dataholder.vrSettings.autoSprint) {
                    if (f >= dataholder.vrSettings.autoSprintThreshold) {
                        minecraft.player.setSprinting(true);
                        if (!this.vivecraft$autoSprintActive) {
                            sprint = true;
                        }
                        this.vivecraft$autoSprintActive = true;
                        this.forwardImpulse = 1.0F;
                    } else if (this.forwardImpulse > 0.0F && dataholder.vrSettings.analogMovement) {
                        this.forwardImpulse = this.forwardImpulse / dataholder.vrSettings.autoSprintThreshold;
                    }
                }
            }
        }

        if (!flag1 && this.vivecraft$movementSetByAnalog) {
            VRInputAction.setKeyBindState(this.options.keyUp, false);
            VRInputAction.setKeyBindState(this.options.keyDown, false);
            VRInputAction.setKeyBindState(this.options.keyLeft, false);
            VRInputAction.setKeyBindState(this.options.keyRight, false);
        }

        this.vivecraft$movementSetByAnalog = flag1;

        if (this.vivecraft$autoSprintActive && f < dataholder.vrSettings.autoSprintThreshold) {
            minecraft.player.setSprinting(false);
            this.vivecraft$autoSprintActive = false;
        }

        boolean flag2 = minecraft.screen == null && (dataholder.vrPlayer.getFreeMove() || dataholder.vrSettings.simulateFalling) && !climbing;
        jumping = this.options.keyJump.isDown() && flag2;
        shiftKeyDown = (dataholder.sneakTracker.sneakCounter > 0 || dataholder.sneakTracker.sneakOverride || this.options.keyShift.isDown()) && minecraft.screen == null;

        this.keyPresses = new Input(up, down, left, right, jumping, shiftKeyDown, sprint);
    }
}
