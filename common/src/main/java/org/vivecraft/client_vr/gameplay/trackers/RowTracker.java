package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.math.Quaternion;

public class RowTracker extends Tracker {
    Vec3[] lastUWPs = new Vec3[2];
    public double[] forces = new double[]{0.0D, 0.0D};
    public Vec3[] paddleAngles = new Vec3[]{null, null};
    double transmissionEfficiency = 0.9D;

    public RowTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
    }

    public boolean isActive(LocalPlayer p) {
        if (ClientDataHolderVR.getInstance().vrSettings.seated) {
            return false;
        } else if (!ClientDataHolderVR.getInstance().vrSettings.realisticRowEnabled) {
            return false;
        } else if (p != null && p.isAlive()) {
            if (this.mc.gameMode == null) {
                return false;
            } else if (Minecraft.getInstance().options.keyUp.isDown()) {
                return false;
            } else if (!(p.getVehicle() instanceof Boat)) {
                return false;
            } else {
                return !ClientDataHolderVR.getInstance().bowTracker.isNotched();
            }
        } else {
            return false;
        }
    }

    public boolean isRowing() {
        return this.forces[0] != 0.0D || this.forces[1] != 0.0D;
    }

    public void reset(LocalPlayer player) {
        this.forces[0] = 0.0D;
        this.forces[1] = 0.0D;
        this.paddleAngles[0] = null;
        this.paddleAngles[1] = null;
    }

    public void doProcess(LocalPlayer player) {
        Boat boat = (Boat) player.getVehicle();
        Quaternion quaternion = (new Quaternion(boat.getXRot(), -(boat.getYRot() % 360.0F), 0.0F)).normalized();

        for (int i = 0; i <= 1; ++i) {
            this.paddleAngles[i] = quaternion.inverse().multiply(this.getArmToPaddleVector(i, boat));

            if (!this.isPaddleUnderWater(i, boat)) {
                this.forces[i] = 0.0D;
                this.lastUWPs[i] = null;
            } else {
                Vec3 vec3 = this.getArmToPaddleVector(i, boat);
                Vec3 vec31 = this.getAttachmentPoint(i, boat);
                Vec3 vec32 = vec31.add(vec3.normalize()).subtract(boat.position());

                if (this.lastUWPs[i] != null) {
                    Vec3 vec33 = this.lastUWPs[i].subtract(vec32);
                    vec33 = vec33.subtract(boat.getDeltaMovement());
                    Vec3 vec34 = quaternion.multiply(new Vec3(0.0D, 0.0D, 1.0D));
                    double d0 = vec33.dot(vec34) * this.transmissionEfficiency / 5.0D;

                    if ((!(d0 < 0.0D) || !(this.forces[i] > 0.0D)) && (!(d0 > 0.0D) || !(this.forces[i] < 0.0D))) {
                        this.forces[i] = Math.min(Math.max(d0, -0.1D), 0.1D);
                    } else {
                        this.forces[i] = 0.0D;
                    }
                }

                this.lastUWPs[i] = vec32;
            }
        }
    }

    Vec3 getArmToPaddleVector(int paddle, Boat boat) {
        Vec3 vec3 = this.getAttachmentPoint(paddle, boat);
        Vec3 vec31 = this.getAbsArmPos(paddle == 0 ? 1 : 0);
        return vec3.subtract(vec31);
    }

    Vec3 getAttachmentPoint(int paddle, Boat boat) {
        Vec3 vec3 = new Vec3((paddle == 0 ? 9.0F : -9.0F) / 16.0F, 0.625D, 0.1875D);
        Quaternion quaternion = (new Quaternion(boat.getXRot(), -(boat.getYRot() % 360.0F), 0.0F)).normalized();
        return boat.position().add(quaternion.multiply(vec3));
    }

    Vec3 getAbsArmPos(int side) {
        Vec3 vec3 = this.dh.vr.controllerHistory[side].averagePosition(0.1D);
        Quaternion quaternion = new Quaternion(0.0F, VRSettings.inst.worldRotation, 0.0F);
        return VRPlayer.get().roomOrigin.add(quaternion.multiply(vec3));
    }

    boolean isPaddleUnderWater(int paddle, Boat boat) {
        Vec3 vec3 = this.getAttachmentPoint(paddle, boat);
        Vec3 vec31 = this.getArmToPaddleVector(paddle, boat).normalize();
        BlockPos blockpos = new BlockPos(vec3.add(vec31));
        return boat.level.getBlockState(blockpos).getMaterial().isLiquid();
    }
}
