package org.vivecraft.mixin.client_vr.world;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.BoatExtension;

@Mixin(Boat.class)
public abstract class BoatMixin extends Entity implements BoatExtension {

    @Shadow
    private float deltaRotation;
    @Shadow
    private boolean inputLeft;
    @Shadow
    private boolean inputRight;
    @Shadow
    private boolean inputUp;

    @Shadow
    public abstract void setPaddleState(boolean pLeft, boolean pRight);

    public Vec3[] paddleAngles = new Vec3[]{null, null};

    public BoatMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyConstant(constant = @Constant(floatValue = 1F, ordinal = 0), method = "controlBoat()V")
    public float vivecraft$inputLeft(float f) {
        Minecraft minecraft = Minecraft.getInstance();
        float f1 = minecraft.player.input.leftImpulse;
        return f1;
    }

    @ModifyConstant(constant = @Constant(floatValue = 1F, ordinal = 1), method = "controlBoat()V")
    public float vivecraft$inputRight(float f) {
        Minecraft minecraft = Minecraft.getInstance();
        float f1 = minecraft.player.input.leftImpulse;
        return -f1;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", shift = At.Shift.BEFORE), method = "controlBoat", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void vivecraft$roomscaleRowing(CallbackInfo ci, float f) {
        if (!VRState.vrRunning) {
            return;
        }

        double mx, mz;
        ClientDataHolderVR clientDataHolderVR = ClientDataHolderVR.getInstance();

        if (this.inputUp && !clientDataHolderVR.vrSettings.seated) {
            //controller-based
            float yaw = clientDataHolderVR.vrPlayer.vrdata_world_pre.getController(1).getYaw();
            if (clientDataHolderVR.vrSettings.vehicleRotation) {
                //tank controls
                float end = this.getYRot() % 360;
                float start = yaw;
                float difference = Math.abs(end - start);

                if (difference > 180) {
                    if (end > start) {
                        start += 360;
                    } else {
                        end += 360;
                    }
                }

                difference = end - start;

                f = 0;

                if (Math.abs(difference) < 30) {
                    f = 0.04f;
                } else if (Math.abs(difference) > 150) {
                    f = -0.005F;
                } else if (difference < 0) {
                    this.deltaRotation += 1;
                    f = 0.005f;
                } else if (difference > 0) {
                    this.deltaRotation -= 1;
                    f = 0.005f;
                }

                mx = Math.sin(-this.getYRot() * 0.017453292F) * f;
                mz = Math.cos(this.getYRot() * 0.017453292F) * f;
            } else {
                //point to move
                mx = Math.sin(-yaw * 0.017453292F) * f;
                mz = Math.cos(yaw * 0.017453292F) * f;
                this.setYRot(yaw);
            }
        } else {
            //roomscale or vanilla behavior
            if (clientDataHolderVR.rowTracker.isRowing() && !clientDataHolderVR.vrSettings.seated) {

                this.deltaRotation += clientDataHolderVR.rowTracker.forces[0] * 50;
                this.deltaRotation -= clientDataHolderVR.rowTracker.forces[1] * 50;

                f = (float) (clientDataHolderVR.rowTracker.forces[0] + clientDataHolderVR.rowTracker.forces[1]);

                mx = Math.sin(-this.getYRot() * 0.017453292F) * f;
                mz = Math.cos(this.getYRot() * 0.017453292F) * f;

                this.inputLeft = clientDataHolderVR.rowTracker.paddleInWater[0] && !clientDataHolderVR.rowTracker.paddleInWater[1];
                this.inputRight = clientDataHolderVR.rowTracker.paddleInWater[1] && !clientDataHolderVR.rowTracker.paddleInWater[0];
                this.inputUp = clientDataHolderVR.rowTracker.paddleInWater[0] || clientDataHolderVR.rowTracker.paddleInWater[1];

                this.paddleAngles[0] = clientDataHolderVR.rowTracker.paddleAngles[0];
                this.paddleAngles[1] = clientDataHolderVR.rowTracker.paddleAngles[1];
            } else {
                //default boat (seated mode)
                mx = Math.sin(-this.getYRot() * 0.017453292F) * f;
                mz = Math.cos(this.getYRot() * 0.017453292F) * f;
            }
        }
        this.setDeltaMovement(this.getDeltaMovement().x + mx, this.getDeltaMovement().y, this.getDeltaMovement().z + mz);

        this.setPaddleState(this.inputRight && !this.inputLeft || this.inputUp, this.inputLeft && !this.inputRight || this.inputUp);
        ci.cancel();
    }

    @Inject(at = @At(value = "HEAD"), method = "tick()V")
    private void vivecraft$clearPaddleAngles(CallbackInfo ci) {
        this.paddleAngles[0] = null;
        this.paddleAngles[1] = null;
    }

    @Override
    public Vec3[] vivecraft$getPaddleAngles() {
        return this.paddleAngles;
    }
}
