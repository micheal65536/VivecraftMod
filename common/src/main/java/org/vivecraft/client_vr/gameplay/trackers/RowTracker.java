package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.math.Quaternion;

public class RowTracker extends Tracker {
    Vec3[] lastUWPs = new Vec3[2];
    public double[] forces = new double[]{0.0D, 0.0D};
    public Vec3[] paddleAngles = new Vec3[]{null, null};
    public boolean[] paddleInWater = new boolean[]{false, false};
    double transmissionEfficiency = 1.0D;
    private int[] hands = new int[2];

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
        return this.paddleAngles[0] != null || this.paddleAngles[1] != null;
    }

    public void reset(LocalPlayer player) {
        this.forces[0] = 0.0D;
        this.forces[1] = 0.0D;
        this.paddleAngles[0] = null;
        this.paddleAngles[1] = null;
        this.paddleInWater[0] = false;
        this.paddleInWater[1] = false;
    }

    public void doProcess(LocalPlayer player) {
        Boat boat = (Boat) player.getVehicle();
        Quaternion quaternion = (new Quaternion(boat.getXRot(), -(boat.getYRot() % 360.0F), 0.0F)).normalized();

        Vec3 hand0 = quaternion.inverse().multiply(this.getAbsArmPos(0).subtract(boat.position()));
        Vec3 hand1 = quaternion.inverse().multiply(this.getAbsArmPos(1).subtract(boat.position()));
        if (hand0.x > hand1.x) {
            hands[0] = 0;
            hands[1] = 1;
        } else {
            hands[0] = 1;
            hands[1] = 0;
        }

        for (int paddle = 0; paddle <= 1; ++paddle) {
            this.paddleAngles[paddle] = quaternion.inverse().multiply(this.getArmToPaddleVector(paddle, hands[paddle], boat));

            boolean inWater;
            if (!this.isPaddleUnderWater(paddle, hands[paddle], boat)) {
                inWater = false;

                this.forces[paddle] = 0.0D;
                this.lastUWPs[paddle] = null;
            } else {
                inWater = true;

                Vec3 vec3 = this.getArmToPaddleVector(paddle, hands[paddle], boat);
                Vec3 vec31 = this.getAttachmentPoint(paddle, boat);
                Vec3 vec32 = vec31.add(vec3.normalize());

                if (this.lastUWPs[paddle] != null) {
                    Vec3 vec33 = this.lastUWPs[paddle].subtract(vec32);
                    Vec3 vec34 = quaternion.multiply(new Vec3(0.0D, 0.0D, 1.0D));
                    double d0 = vec33.dot(vec34) * this.transmissionEfficiency / 5.0D;

                    if ((!(d0 < 0.0D) || !(this.forces[paddle] > 0.0D)) && (!(d0 > 0.0D) || !(this.forces[paddle] < 0.0D))) {
                        this.forces[paddle] = Math.min(Math.max(d0, -0.1D), 0.1D);
                    } else {
                        this.forces[paddle] = 0.0D;
                    }
                }

                this.lastUWPs[paddle] = vec32;
            }

            if (inWater) {
                if (!this.paddleInWater[paddle]) {
                    this.dh.vr.triggerHapticPulse(ControllerType.values()[hands[paddle]], 0.05F, 100.0F, 0.8F);
                } else {
                    float strength = (float) (Math.abs(this.forces[paddle]) / 0.1D);
                    if (strength > 0.05F) {
                        strength = strength * 0.7F + 0.3F;
                        this.dh.vr.triggerHapticPulse(ControllerType.values()[hands[paddle]], 0.05F, 100.0F, strength);
                    }
                }
            } else {
                if (this.paddleInWater[paddle]) {
                    this.dh.vr.triggerHapticPulse(ControllerType.values()[hands[paddle]], 0.05F, 100.0F, 0.2F);
                }
            }

            this.paddleInWater[paddle] = inWater;
        }
    }

    Vec3 getArmToPaddleVector(int paddle, int hand, Boat boat) {
        Vec3 vec3 = this.getAttachmentPoint(paddle, boat);
        Vec3 vec31 = this.getAbsArmPos(hand);
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

    boolean isPaddleUnderWater(int paddle, int hand, Boat boat) {
        Vec3 vec3 = this.getAttachmentPoint(paddle, boat);
        Vec3 vec31 = this.getArmToPaddleVector(paddle, hand, boat).normalize();
        Vec3 vec32 = vec3.add(vec31);
        return vec32.subtract(boat.position()).y < 0.2F;
    }
}
